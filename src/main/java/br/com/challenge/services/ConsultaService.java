package br.com.challenge.services;

import br.com.challenge.dtos.ConsultaRequestDTO;
import br.com.challenge.exceptions.BusinessRuleException;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.Consulta;
import br.com.challenge.repositories.ConsultaRepository;
import br.com.challenge.repositories.PkgClyvoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    // Chamadas ao package PL/SQL PKG_CLYVO
    @Autowired
    private PkgClyvoRepository pkgClyvo;

    // 1. LISTAR CONSULTAS COM PAGINAÇÃO
    public Page<Consulta> listarTodas(Pageable pageable) {
        return consultaRepository.findAll(pageable);
    }

    // 2. BUSCAR CONSULTA POR ID
    public Consulta buscarPorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Consulta não encontrada com o ID: " + id
                        )
                );
    }

    // 3. AGENDAR CONSULTA
    // As regras de negócio (veterinário e clínica ativos, pet pertence ao
    // responsável, data futura, conflito de horário e limite diário de
    // consultas) são validadas pela procedure pkg_clyvo.agendar_consulta.
    // A trigger de auditoria registra o INSERT com o usuário logado.
    @Transactional
    public Consulta agendar(ConsultaRequestDTO dto) {
        Long id = pkgClyvo.executarComoUsuario(
                usuarioLogado(),
                () -> pkgClyvo.agendarConsulta(dto)
        );

        return buscarPorId(id);
    }

    // 4. REGISTRAR DIAGNÓSTICO E FINALIZAR CONSULTA
    @Transactional
    public Consulta atualizarDiagnostico(
            Long id,
            String diagnostico,
            LocalDateTime retorno
    ) {
        Consulta consulta = buscarPorId(id);

        if ("CANCELADA".equalsIgnoreCase(consulta.getStatus())) {
            throw new BusinessRuleException(
                    "Não é possível registrar diagnóstico em uma consulta cancelada."
            );
        }

        if ("REALIZADA".equalsIgnoreCase(consulta.getStatus())) {
            throw new BusinessRuleException(
                    "Esta consulta já foi finalizada."
            );
        }

        consulta.setDiagnostico(diagnostico);
        consulta.setRetornoPrevisto(retorno);
        consulta.setStatus("REALIZADA");

        // saveAndFlush: o UPDATE acontece agora, enquanto o usuário
        // logado está identificado na sessão (para a auditoria)
        return pkgClyvo.executarComoUsuario(
                usuarioLogado(),
                () -> consultaRepository.saveAndFlush(consulta)
        );
    }

    // 5. CANCELAR CONSULTA
    @Transactional
    public void cancelar(Long id) {
        Consulta consulta = buscarPorId(id);

        if ("REALIZADA".equalsIgnoreCase(consulta.getStatus())) {
            throw new BusinessRuleException(
                    "Não é possível cancelar uma consulta já realizada."
            );
        }

        if ("CANCELADA".equalsIgnoreCase(consulta.getStatus())) {
            throw new BusinessRuleException(
                    "Esta consulta já está cancelada."
            );
        }

        consulta.setStatus("CANCELADA");

        pkgClyvo.executarComoUsuario(
                usuarioLogado(),
                () -> consultaRepository.saveAndFlush(consulta)
        );
    }

    // 6. VERIFICAR DISPONIBILIDADE DO VETERINÁRIO (função do package)
    public String verificarDisponibilidade(Long veterinarioId, LocalDateTime data) {
        return pkgClyvo.validarLimiteAgenda(veterinarioId, data);
    }

    private String usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonimo";
    }
}