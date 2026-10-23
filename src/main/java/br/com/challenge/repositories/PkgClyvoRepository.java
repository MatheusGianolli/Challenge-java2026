package br.com.challenge.repositories;

import br.com.challenge.dtos.ConsultaRequestDTO;
import br.com.challenge.dtos.ResumoConsultaDTO;
import br.com.challenge.exceptions.BusinessRuleException;
import br.com.challenge.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Acesso ao package PL/SQL PKG_CLYVO (migration R__pkg_clyvo.sql).
 *
 * As regras de negócio do agendamento, o relatório de faturamento,
 * a exportação JSON para o MongoDB e a auditoria ficam no banco;
 * esta classe só chama as rotinas e converte os resultados.
 *
 * Os métodos devem ser usados dentro de um @Transactional: assim todas
 * as chamadas usam a mesma conexão, o que é necessário para o usuário
 * da aplicação (CLIENT_IDENTIFIER) chegar até a trigger de auditoria.
 */
@Repository
public class PkgClyvoRepository {

    // Erros de negócio levantados pelo package (RAISE_APPLICATION_ERROR)
    private static final int ERRO_NAO_ENCONTRADO = 20001;
    private static final int ERRO_NEGOCIO_INICIO = 20002;
    private static final int ERRO_NEGOCIO_FIM = 20012;

    private final JdbcTemplate jdbcTemplate;

    public PkgClyvoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ------------------------------------------------------------------
    // Usuário da aplicação gravado na auditoria
    // ------------------------------------------------------------------

    /**
     * Executa a operação identificando na sessão do banco quem está logado
     * na API. A trigger TRG_AUDITORIA_CONSULTAS grava esse nome na coluna
     * USUARIO_APLICACAO. Ao final a identificação é limpa, para não vazar
     * para outra requisição que reutilize a mesma conexão do pool.
     *
     * Atenção: alterações via JPA precisam de flush dentro da operação
     * (ex.: saveAndFlush), senão o UPDATE só acontece no commit, quando
     * a identificação já foi limpa.
     */
    public <T> T executarComoUsuario(String usuario, Supplier<T> operacao) {
        executar(() -> jdbcTemplate.execute((java.sql.Connection con) -> {
            try (CallableStatement cs = con.prepareCall(
                    "{call pkg_clyvo.definir_usuario_aplicacao(?)}")) {
                cs.setString(1, usuario);
                cs.execute();
            }
            return null;
        }));

        try {
            return operacao.get();
        } finally {
            jdbcTemplate.execute("BEGIN DBMS_SESSION.CLEAR_IDENTIFIER; END;");
        }
    }

    // ------------------------------------------------------------------
    // Agenda
    // ------------------------------------------------------------------

    /**
     * Chama pkg_clyvo.agendar_consulta, que valida as regras de negócio
     * (veterinário e clínica ativos, pet do responsável, data futura,
     * conflito de horário, limite diário) e insere a consulta.
     *
     * @return ID da consulta criada
     */
    public Long agendarConsulta(ConsultaRequestDTO dto) {
        return executar(() -> jdbcTemplate.execute(
                "{call pkg_clyvo.agendar_consulta(?, ?, ?, ?, ?, ?, ?, ?, ?)}",
                (CallableStatement cs) -> {
                    cs.setLong(1, dto.responsavelId());
                    cs.setLong(2, dto.petId());
                    cs.setLong(3, dto.clinicaId());
                    cs.setLong(4, dto.veterinarioId());
                    cs.setTimestamp(5, Timestamp.valueOf(dto.dataPrevista()));
                    cs.setString(6, dto.tipoConsulta());
                    cs.setString(7, dto.descricaoSintomas());
                    cs.setBigDecimal(8, dto.valor() != null ? dto.valor() : BigDecimal.ZERO);
                    cs.registerOutParameter(9, Types.NUMERIC);
                    cs.execute();
                    return cs.getLong(9);
                }
        ));
    }

    /**
     * Chama a função pkg_clyvo.valida_limite_agenda.
     *
     * @return "DISPONIVEL (n vagas)" ou "LIMITE_EXCEDIDO"
     */
    public String validarLimiteAgenda(Long veterinarioId, LocalDateTime data) {
        return executar(() -> jdbcTemplate.execute(
                "{? = call pkg_clyvo.valida_limite_agenda(?, ?)}",
                (CallableStatement cs) -> {
                    cs.registerOutParameter(1, Types.VARCHAR);
                    cs.setLong(2, veterinarioId);
                    cs.setTimestamp(3, Timestamp.valueOf(data));
                    cs.execute();
                    return cs.getString(1);
                }
        ));
    }

    // ------------------------------------------------------------------
    // Relatório
    // ------------------------------------------------------------------

    /**
     * Chama pkg_clyvo.resumo_consultas, que devolve um SYS_REFCURSOR com o
     * faturamento das consultas realizadas por veterinário e tipo,
     * subtotais por veterinário e o total geral.
     */
    public List<ResumoConsultaDTO> resumoConsultas(LocalDate inicio, LocalDate fim) {
        return executar(() -> jdbcTemplate.execute(
                "{call pkg_clyvo.resumo_consultas(?, ?, ?)}",
                (CallableStatement cs) -> {
                    cs.setDate(1, inicio != null ? Date.valueOf(inicio) : null);
                    cs.setDate(2, fim != null ? Date.valueOf(fim) : null);
                    cs.registerOutParameter(3, Types.REF_CURSOR);
                    cs.execute();

                    List<ResumoConsultaDTO> linhas = new ArrayList<>();
                    try (ResultSet rs = cs.getObject(3, ResultSet.class)) {
                        while (rs.next()) {
                            // Na linha TOTAL_GERAL o veterinário vem nulo
                            long vetIdLido = rs.getLong("VETERINARIO_ID");
                            Long vetId = rs.wasNull() ? null : vetIdLido;

                            linhas.add(new ResumoConsultaDTO(
                                    rs.getString("TIPO_LINHA"),
                                    vetId,
                                    rs.getString("VETERINARIO"),
                                    rs.getString("TIPO_CONSULTA"),
                                    rs.getLong("QUANTIDADE"),
                                    rs.getBigDecimal("VALOR_TOTAL")
                            ));
                        }
                    }
                    return linhas;
                }
        ));
    }

    // ------------------------------------------------------------------
    // Exportação para o MongoDB
    // ------------------------------------------------------------------

    /**
     * Chama pkg_clyvo.exportar_colecao_json e devolve o array JSON
     * (formato aceito pelo mongoimport --jsonArray).
     *
     * @param colecao TUTORES, VETERINARIOS ou CLINICAS
     */
    public String exportarColecaoJson(String colecao) {
        return executar(() -> jdbcTemplate.execute(
                "{? = call pkg_clyvo.exportar_colecao_json(?)}",
                (CallableStatement cs) -> {
                    cs.registerOutParameter(1, Types.CLOB);
                    cs.setString(2, colecao);
                    cs.execute();

                    Clob clob = cs.getClob(1);
                    try {
                        return clob.getSubString(1, (int) clob.length());
                    } finally {
                        clob.free();
                    }
                }
        ));
    }

    // ------------------------------------------------------------------
    // Tradução dos erros do package para as exceções da API
    // ------------------------------------------------------------------

    private <T> T executar(Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (DataAccessException ex) {
            SQLException sql = encontrarSqlException(ex);
            if (sql == null) {
                throw ex;
            }

            int codigo = sql.getErrorCode();
            String mensagem = mensagemDoBanco(sql);

            if (codigo == ERRO_NAO_ENCONTRADO) {
                throw new ResourceNotFoundException(mensagem);
            }
            if (codigo >= ERRO_NEGOCIO_INICIO && codigo <= ERRO_NEGOCIO_FIM) {
                throw new BusinessRuleException(mensagem);
            }
            throw ex;
        }
    }

    private SQLException encontrarSqlException(Throwable ex) {
        Throwable atual = ex;
        while (atual != null) {
            if (atual instanceof SQLException sql) {
                return sql;
            }
            atual = atual.getCause();
        }
        return null;
    }

    // "ORA-20006: O veterinario ja possui...\nORA-06512: at ..." -> "O veterinario ja possui..."
    private String mensagemDoBanco(SQLException sql) {
        String texto = sql.getMessage() == null ? "" : sql.getMessage();
        String primeiraLinha = texto.split("\\R", 2)[0];
        int separador = primeiraLinha.indexOf(": ");
        return separador >= 0 ? primeiraLinha.substring(separador + 2).trim() : primeiraLinha.trim();
    }
}
