-- =====================================================================
-- R__pkg_clyvo.sql  (migration REPETIVEL do Flyway)
-- Reexecutada automaticamente sempre que este arquivo mudar.
--
-- Conteudo:
--   PKG_CLYVO (spec + body)
--     - valida_limite_agenda / vagas_disponiveis  (regra de negocio)
--     - agendar_consulta                         (insert via aplicacao)
--     - consulta_para_json                       (JSON de uma consulta)
--     - exportar_colecao_json                    (dataset p/ MongoDB)
--     - resumo_consultas                         (relatorio p/ API)
--     - definir_usuario_aplicacao                (rastreio na auditoria)
--     - registrar_auditoria                      (usado pela trigger)
--   TRG_AUDITORIA_CONSULTAS
--
-- Codigos de erro (RAISE_APPLICATION_ERROR):
--   -20001 registro inexistente        -20007 limite diario excedido
--   -20002 veterinario inativo         -20008 valor negativo
--   -20003 clinica inativa             -20009 parametro obrigatorio nulo
--   -20004 pet nao pertence ao tutor   -20010 colecao invalida
--   -20005 data no passado             -20011 periodo invalido
--   -20006 conflito de horario         -20012 falha na auditoria
--   -20099 erro inesperado
--
-- As procedures NAO fazem COMMIT: a transacao e controlada por quem
-- chama (Spring/@Transactional ou o usuario no SQL Developer).
-- =====================================================================

CREATE OR REPLACE PACKAGE pkg_clyvo AS

    c_limite_diario CONSTANT PLS_INTEGER := 8;

    -- Quantas vagas o veterinario ainda tem no dia de p_data
    FUNCTION vagas_disponiveis (
        p_veterinario_id IN NUMBER,
        p_data           IN TIMESTAMP
    ) RETURN NUMBER;

    -- 'DISPONIVEL (n vagas)' ou 'LIMITE_EXCEDIDO'
    FUNCTION valida_limite_agenda (
        p_veterinario_id IN NUMBER,
        p_data           IN TIMESTAMP
    ) RETURN VARCHAR2;

    -- Valida todas as regras de negocio e insere a consulta
    PROCEDURE agendar_consulta (
        p_responsavel_id IN  NUMBER,
        p_pet_id         IN  NUMBER,
        p_clinica_id     IN  NUMBER,
        p_veterinario_id IN  NUMBER,
        p_data_prevista  IN  TIMESTAMP,
        p_tipo_consulta  IN  VARCHAR2,
        p_sintomas       IN  VARCHAR2,
        p_valor          IN  NUMBER,
        p_consulta_id    OUT NUMBER
    );

    -- Documento JSON de uma consulta (com veterinario e clinica embutidos)
    FUNCTION consulta_para_json (
        p_consulta_id IN NUMBER
    ) RETURN CLOB;

    -- Array JSON pronto para mongoimport --jsonArray
    -- p_colecao: 'TUTORES', 'VETERINARIOS' ou 'CLINICAS'
    FUNCTION exportar_colecao_json (
        p_colecao IN VARCHAR2
    ) RETURN CLOB;

    -- Faturamento de consultas REALIZADAS por veterinario e tipo,
    -- com linhas DETALHE, SUBTOTAL (por veterinario) e TOTAL_GERAL
    PROCEDURE resumo_consultas (
        p_data_inicio IN  DATE DEFAULT NULL,
        p_data_fim    IN  DATE DEFAULT NULL,
        p_cursor      OUT SYS_REFCURSOR
    );

    -- Grava o usuario logado na API na sessao (lido pela auditoria)
    PROCEDURE definir_usuario_aplicacao (
        p_usuario IN VARCHAR2
    );

    -- Chamado pela trigger de auditoria
    PROCEDURE registrar_auditoria (
        p_consulta_id IN NUMBER,
        p_operacao    IN VARCHAR2,
        p_anteriores  IN VARCHAR2,
        p_novos       IN VARCHAR2
    );

END pkg_clyvo;
/

CREATE OR REPLACE PACKAGE BODY pkg_clyvo AS

    -- Excecoes do Oracle tratadas por nome
    e_fk_violada    EXCEPTION;
    PRAGMA EXCEPTION_INIT(e_fk_violada, -2291);
    e_check_violado EXCEPTION;
    PRAGMA EXCEPTION_INIT(e_check_violado, -2290);

    c_nls_numero CONSTANT VARCHAR2(40) := 'NLS_NUMERIC_CHARACTERS=''.,''';

    -- =================================================================
    -- Auxiliares privados
    -- =================================================================

    -- Erros de negocio (-20000 a -20999) sobem intactos;
    -- qualquer outro erro vira -20099 com a mensagem original
    PROCEDURE relancar (p_contexto IN VARCHAR2) IS
    BEGIN
        IF SQLCODE BETWEEN -20999 AND -20000 THEN
            RAISE_APPLICATION_ERROR(SQLCODE, SUBSTR(SQLERRM, 12), FALSE);
        ELSE
            RAISE_APPLICATION_ERROR(-20099,
                'Erro inesperado em ' || p_contexto || ': ' || SQLERRM);
        END IF;
    END relancar;

    PROCEDURE exigir (p_valor IN VARCHAR2, p_nome IN VARCHAR2) IS
    BEGIN
        IF p_valor IS NULL THEN
            RAISE_APPLICATION_ERROR(-20009, 'Parametro obrigatorio nao informado: ' || p_nome);
        END IF;
    END exigir;

    -- Escapa caracteres especiais (a barra invertida primeiro)
    FUNCTION escapar (p_texto IN VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                   p_texto,
                   '\', '\\'),
                   '"', '\"'),
                   CHR(13), '\r'),
                   CHR(10), '\n'),
                   CHR(9), '\t');
    END escapar;

    FUNCTION j_txt (p_texto IN VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        IF p_texto IS NULL THEN
            RETURN 'null';
        END IF;
        RETURN '"' || escapar(p_texto) || '"';
    END j_txt;

    -- Numero sempre com ponto decimal, independente do NLS da sessao
    FUNCTION j_num (p_numero IN NUMBER) RETURN VARCHAR2 IS
        v_txt VARCHAR2(100);
    BEGIN
        IF p_numero IS NULL THEN
            RETURN 'null';
        END IF;
        v_txt := TO_CHAR(p_numero, 'TM9', c_nls_numero);
        IF SUBSTR(v_txt, 1, 1) = '.' THEN
            v_txt := '0' || v_txt;
        ELSIF SUBSTR(v_txt, 1, 2) = '-.' THEN
            v_txt := '-0' || SUBSTR(v_txt, 2);
        END IF;
        RETURN v_txt;
    END j_num;

    -- Data no formato Extended JSON do MongoDB: {"$date": "...-03:00"}
    FUNCTION j_data (p_data IN TIMESTAMP) RETURN VARCHAR2 IS
    BEGIN
        IF p_data IS NULL THEN
            RETURN 'null';
        END IF;
        RETURN '{"$date": "' || TO_CHAR(p_data, 'YYYY-MM-DD"T"HH24:MI:SS') || '.000-03:00"}';
    END j_data;

    -- 'S'/'N' vira booleano JSON
    FUNCTION j_bool (p_flag IN VARCHAR2) RETURN VARCHAR2 IS
    BEGIN
        RETURN CASE UPPER(p_flag)
                   WHEN 'S' THEN 'true'
                   WHEN 'N' THEN 'false'
                   ELSE 'null'
               END;
    END j_bool;

    PROCEDURE add (p_clob IN OUT NOCOPY CLOB, p_texto IN VARCHAR2) IS
    BEGIN
        IF p_texto IS NOT NULL THEN
            DBMS_LOB.WRITEAPPEND(p_clob, LENGTH(p_texto), p_texto);
        END IF;
    END add;

    PROCEDURE add_clob (p_clob IN OUT NOCOPY CLOB, p_parte IN CLOB) IS
    BEGIN
        IF p_parte IS NOT NULL AND DBMS_LOB.GETLENGTH(p_parte) > 0 THEN
            DBMS_LOB.APPEND(p_clob, p_parte);
        END IF;
    END add_clob;

    FUNCTION novo_clob RETURN CLOB IS
        v_clob CLOB;
    BEGIN
        DBMS_LOB.CREATETEMPORARY(v_clob, TRUE, DBMS_LOB.CALL);
        RETURN v_clob;
    END novo_clob;

    -- =================================================================
    -- Regras de agenda
    -- =================================================================

    FUNCTION vagas_disponiveis (
        p_veterinario_id IN NUMBER,
        p_data           IN TIMESTAMP
    ) RETURN NUMBER IS
        v_existe NUMBER;
        v_total  NUMBER;
        v_dia    DATE;
    BEGIN
        exigir(TO_CHAR(p_veterinario_id), 'p_veterinario_id');
        exigir(TO_CHAR(p_data), 'p_data');

        SELECT COUNT(*) INTO v_existe
          FROM tb_veterinarios
         WHERE id = p_veterinario_id;

        IF v_existe = 0 THEN
            RAISE_APPLICATION_ERROR(-20001,
                'Veterinario ' || p_veterinario_id || ' nao encontrado.');
        END IF;

        -- Intervalo [dia, dia+1) para aproveitar o indice (veterinario_id, data_prevista)
        v_dia := TRUNC(CAST(p_data AS DATE));

        SELECT COUNT(*) INTO v_total
          FROM tb_consultas
         WHERE veterinario_id = p_veterinario_id
           AND data_prevista >= v_dia
           AND data_prevista <  v_dia + 1
           AND status <> 'CANCELADA';

        RETURN GREATEST(c_limite_diario - v_total, 0);
    EXCEPTION
        WHEN OTHERS THEN
            relancar('vagas_disponiveis');
    END vagas_disponiveis;

    FUNCTION valida_limite_agenda (
        p_veterinario_id IN NUMBER,
        p_data           IN TIMESTAMP
    ) RETURN VARCHAR2 IS
        v_vagas NUMBER;
    BEGIN
        v_vagas := vagas_disponiveis(p_veterinario_id, p_data);

        IF v_vagas = 0 THEN
            RETURN 'LIMITE_EXCEDIDO';
        END IF;
        RETURN 'DISPONIVEL (' || v_vagas || ' vagas)';
    END valida_limite_agenda;

    PROCEDURE agendar_consulta (
        p_responsavel_id IN  NUMBER,
        p_pet_id         IN  NUMBER,
        p_clinica_id     IN  NUMBER,
        p_veterinario_id IN  NUMBER,
        p_data_prevista  IN  TIMESTAMP,
        p_tipo_consulta  IN  VARCHAR2,
        p_sintomas       IN  VARCHAR2,
        p_valor          IN  NUMBER,
        p_consulta_id    OUT NUMBER
    ) IS
        v_status_vet     tb_veterinarios.status%TYPE;
        v_status_clinica tb_clinicas.status%TYPE;
        v_dono_pet       tb_pets.responsavel_id%TYPE;
        v_conflitos      NUMBER;
    BEGIN
        exigir(TO_CHAR(p_responsavel_id), 'p_responsavel_id');
        exigir(TO_CHAR(p_pet_id),         'p_pet_id');
        exigir(TO_CHAR(p_clinica_id),     'p_clinica_id');
        exigir(TO_CHAR(p_veterinario_id), 'p_veterinario_id');
        exigir(TO_CHAR(p_data_prevista),  'p_data_prevista');
        exigir(p_tipo_consulta,           'p_tipo_consulta');

        IF NVL(p_valor, 0) < 0 THEN
            RAISE_APPLICATION_ERROR(-20008, 'O valor da consulta nao pode ser negativo.');
        END IF;

        IF p_data_prevista <= SYSTIMESTAMP THEN
            RAISE_APPLICATION_ERROR(-20005, 'A data prevista deve ser no futuro.');
        END IF;

        -- Cada busca em bloco proprio para dizer exatamente o que faltou
        BEGIN
            SELECT status INTO v_status_vet
              FROM tb_veterinarios WHERE id = p_veterinario_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20001, 'Veterinario ' || p_veterinario_id || ' nao encontrado.');
        END;

        BEGIN
            SELECT status INTO v_status_clinica
              FROM tb_clinicas WHERE id = p_clinica_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20001, 'Clinica ' || p_clinica_id || ' nao encontrada.');
        END;

        BEGIN
            SELECT responsavel_id INTO v_dono_pet
              FROM tb_pets WHERE id = p_pet_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20001, 'Pet ' || p_pet_id || ' nao encontrado.');
        END;

        IF NVL(v_status_vet, 'X') <> 'ATIVO' THEN
            RAISE_APPLICATION_ERROR(-20002, 'Nao e possivel agendar com veterinario inativo.');
        END IF;

        IF NVL(v_status_clinica, 'X') <> 'ATIVO' THEN
            RAISE_APPLICATION_ERROR(-20003, 'Nao e possivel agendar em clinica inativa.');
        END IF;

        -- Garante a consistencia da coluna redundante responsavel_id
        IF v_dono_pet <> p_responsavel_id THEN
            RAISE_APPLICATION_ERROR(-20004,
                'O pet ' || p_pet_id || ' nao pertence ao responsavel ' || p_responsavel_id || '.');
        END IF;

        SELECT COUNT(*) INTO v_conflitos
          FROM tb_consultas
         WHERE veterinario_id = p_veterinario_id
           AND data_prevista  = p_data_prevista
           AND status <> 'CANCELADA';

        IF v_conflitos > 0 THEN
            RAISE_APPLICATION_ERROR(-20006, 'O veterinario ja possui consulta neste horario.');
        END IF;

        IF vagas_disponiveis(p_veterinario_id, p_data_prevista) = 0 THEN
            RAISE_APPLICATION_ERROR(-20007,
                'Limite de ' || c_limite_diario || ' consultas diarias do veterinario atingido.');
        END IF;

        INSERT INTO tb_consultas (
            responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
            data_prevista, tipo_consulta, descricao_sintomas, status, valor
        ) VALUES (
            p_responsavel_id, p_pet_id, p_clinica_id, p_veterinario_id, SYSTIMESTAMP,
            p_data_prevista, p_tipo_consulta, p_sintomas, 'AGENDADA', NVL(p_valor, 0)
        )
        RETURNING id INTO p_consulta_id;

    EXCEPTION
        WHEN e_fk_violada THEN
            RAISE_APPLICATION_ERROR(-20001,
                'Registro relacionado nao existe (removido durante o agendamento?).');
        WHEN e_check_violado THEN
            RAISE_APPLICATION_ERROR(-20008,
                'Dados violam uma regra da tabela de consultas: ' || SQLERRM);
        WHEN OTHERS THEN
            relancar('agendar_consulta');
    END agendar_consulta;

    -- =================================================================
    -- JSON
    -- =================================================================

    FUNCTION consulta_para_json (
        p_consulta_id IN NUMBER
    ) RETURN CLOB IS
        v_json CLOB;
    BEGIN
        exigir(TO_CHAR(p_consulta_id), 'p_consulta_id');
        v_json := novo_clob;

        FOR r IN (
            SELECT c.id, c.data_prevista, c.retorno_previsto, c.tipo_consulta,
                   c.descricao_sintomas, c.diagnostico, c.status, c.valor,
                   v.id AS vet_id, v.nome AS vet_nome, v.crmv AS vet_crmv,
                   v.especialidade AS vet_especialidade,
                   l.id AS cli_id, l.nome AS cli_nome
              FROM tb_consultas c
              JOIN tb_veterinarios v ON v.id = c.veterinario_id
              JOIN tb_clinicas     l ON l.id = c.clinica_id
             WHERE c.id = p_consulta_id
        ) LOOP
            add(v_json, '{"consultaId": ' || j_num(r.id)
                || ', "dataPrevista": '   || j_data(r.data_prevista)
                || ', "retornoPrevisto": '|| j_data(r.retorno_previsto)
                || ', "tipo": '           || j_txt(r.tipo_consulta)
                || ', "status": '         || j_txt(r.status)
                || ', "valor": '          || j_num(r.valor));
            add(v_json, ', "sintomas": '  || j_txt(r.descricao_sintomas));
            add(v_json, ', "diagnostico": '|| j_txt(r.diagnostico));
            add(v_json, ', "veterinario": {"id": ' || j_num(r.vet_id)
                || ', "nome": '          || j_txt(r.vet_nome)
                || ', "crmv": '          || j_txt(r.vet_crmv)
                || ', "especialidade": ' || j_txt(r.vet_especialidade) || '}'
                || ', "clinica": {"id": ' || j_num(r.cli_id)
                || ', "nome": '          || j_txt(r.cli_nome) || '}}');
        END LOOP;

        IF DBMS_LOB.GETLENGTH(v_json) = 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Consulta ' || p_consulta_id || ' nao encontrada.');
        END IF;

        RETURN v_json;
    EXCEPTION
        WHEN OTHERS THEN
            relancar('consulta_para_json');
    END consulta_para_json;

    -- Colecao TUTORES: tutor -> pets[] -> consultas[] (modelo embutido)
    FUNCTION json_tutores RETURN CLOB IS
        v_json         CLOB := novo_clob;
        v_primeiro_tut BOOLEAN := TRUE;
        v_primeiro_pet BOOLEAN;
        v_primeira_con BOOLEAN;
    BEGIN
        add(v_json, '[' || CHR(10));

        FOR t IN (SELECT * FROM tb_tutores ORDER BY id) LOOP
            IF NOT v_primeiro_tut THEN
                add(v_json, ',' || CHR(10));
            END IF;
            v_primeiro_tut := FALSE;

            add(v_json, '{"_id": ' || j_num(t.id)
                || ', "nome": '            || j_txt(t.nome)
                || ', "documento": {"tipo": ' || j_txt(t.tipo_documento)
                || ', "numero": '          || j_txt(t.numero_documento) || '}'
                || ', "dataNascimento": '  || j_data(t.data_nascimento)
                || ', "tipoResponsavel": ' || j_txt(t.tipo_responsavel));
            add(v_json, ', "contato": {"email": ' || j_txt(t.email)
                || ', "telefone": '        || j_txt(t.telefone)
                || ', "preferencia": '     || j_txt(t.preferencia_contato)
                || ', "recebeNotificacao": '    || j_bool(t.recebe_notificacao)
                || ', "autorizaMonitoramento": '|| j_bool(t.autoriza_monitoramento) || '}');
            add(v_json, ', "endereco": {"logradouro": ' || j_txt(t.logradouro)
                || ', "numero": ' || j_txt(t.numero_endereco)
                || ', "bairro": ' || j_txt(t.bairro)
                || ', "cidade": ' || j_txt(t.cidade)
                || ', "estado": ' || j_txt(t.estado)
                || ', "cep": '    || j_txt(t.cep) || '}'
                || ', "status": ' || j_txt(t.status)
                || ', "dataCadastro": ' || j_data(t.data_cadastro)
                || ', "pets": [');

            v_primeiro_pet := TRUE;
            FOR p IN (SELECT * FROM tb_pets WHERE responsavel_id = t.id ORDER BY id) LOOP
                IF NOT v_primeiro_pet THEN
                    add(v_json, ', ');
                END IF;
                v_primeiro_pet := FALSE;

                add(v_json, '{"petId": ' || j_num(p.id)
                    || ', "nome": '        || j_txt(p.nome)
                    || ', "especie": '     || j_txt(p.especie)
                    || ', "raca": '        || j_txt(p.raca)
                    || ', "dataNascimento": ' || j_data(p.data_nascimento)
                    || ', "sexo": '        || j_txt(p.sexo)
                    || ', "porte": '       || j_txt(p.porte));
                add(v_json, ', "peso": '   || j_num(p.peso)
                    || ', "altura": '      || j_num(p.altura)
                    || ', "statusSaude": ' || j_txt(p.status_saude)
                    || ', "alergias": '    || j_txt(p.alergias)
                    || ', "castrado": '    || j_bool(p.castrado)
                    || ', "corPelagem": '  || j_txt(p.cor_pelagem)
                    || ', "microchip": '   || j_txt(p.microchip)
                    || ', "consultas": [');

                v_primeira_con := TRUE;
                FOR c IN (SELECT id FROM tb_consultas WHERE pet_id = p.id ORDER BY data_prevista) LOOP
                    IF NOT v_primeira_con THEN
                        add(v_json, ', ');
                    END IF;
                    v_primeira_con := FALSE;
                    add_clob(v_json, consulta_para_json(c.id));
                END LOOP;

                add(v_json, ']}');
            END LOOP;

            add(v_json, ']}');
        END LOOP;

        add(v_json, CHR(10) || ']');
        RETURN v_json;
    END json_tutores;

    -- Colecao VETERINARIOS: com estatisticas pre-calculadas
    FUNCTION json_veterinarios RETURN CLOB IS
        v_json     CLOB := novo_clob;
        v_primeiro BOOLEAN := TRUE;
    BEGIN
        add(v_json, '[' || CHR(10));

        FOR v IN (
            SELECT v.*,
                   (SELECT COUNT(*) FROM tb_consultas c
                     WHERE c.veterinario_id = v.id) AS total_consultas,
                   (SELECT COUNT(*) FROM tb_consultas c
                     WHERE c.veterinario_id = v.id AND c.status = 'REALIZADA') AS realizadas,
                   (SELECT NVL(SUM(c.valor), 0) FROM tb_consultas c
                     WHERE c.veterinario_id = v.id AND c.status = 'REALIZADA') AS faturamento
              FROM tb_veterinarios v
             ORDER BY v.id
        ) LOOP
            IF NOT v_primeiro THEN
                add(v_json, ',' || CHR(10));
            END IF;
            v_primeiro := FALSE;

            add(v_json, '{"_id": ' || j_num(v.id)
                || ', "nome": '          || j_txt(v.nome)
                || ', "crmv": '          || j_txt(v.crmv)
                || ', "especialidade": ' || j_txt(v.especialidade)
                || ', "tipoAtuacao": '   || j_txt(v.tipo_atuacao)
                || ', "contato": {"email": ' || j_txt(v.email)
                || ', "telefone": '      || j_txt(v.telefone) || '}'
                || ', "status": '        || j_txt(v.status)
                || ', "dataCadastro": '  || j_data(v.data_cadastro)
                || ', "estatisticas": {"totalConsultas": ' || j_num(v.total_consultas)
                || ', "consultasRealizadas": ' || j_num(v.realizadas)
                || ', "faturamentoTotal": '    || j_num(v.faturamento) || '}}');
        END LOOP;

        add(v_json, CHR(10) || ']');
        RETURN v_json;
    END json_veterinarios;

    FUNCTION json_clinicas RETURN CLOB IS
        v_json     CLOB := novo_clob;
        v_primeiro BOOLEAN := TRUE;
    BEGIN
        add(v_json, '[' || CHR(10));

        FOR l IN (SELECT * FROM tb_clinicas ORDER BY id) LOOP
            IF NOT v_primeiro THEN
                add(v_json, ',' || CHR(10));
            END IF;
            v_primeiro := FALSE;

            add(v_json, '{"_id": ' || j_num(l.id)
                || ', "nome": '                 || j_txt(l.nome)
                || ', "cnpj": '                 || j_txt(l.cnpj)
                || ', "tipoEstabelecimento": '  || j_txt(l.tipo_estabelecimento)
                || ', "especialidadePrincipal": '|| j_txt(l.especialidade_principal)
                || ', "atendimento24h": '       || j_bool(l.atendimento_24h)
                || ', "atendeEmergencia": '     || j_bool(l.atende_emergencia));
            add(v_json, ', "contato": {"email": ' || j_txt(l.email)
                || ', "telefone": ' || j_txt(l.telefone) || '}'
                || ', "endereco": {"logradouro": ' || j_txt(l.logradouro)
                || ', "numero": ' || j_txt(l.numero_endereco)
                || ', "bairro": ' || j_txt(l.bairro)
                || ', "cidade": ' || j_txt(l.cidade)
                || ', "estado": ' || j_txt(l.estado)
                || ', "cep": '    || j_txt(l.cep) || '}'
                || ', "status": ' || j_txt(l.status)
                || ', "dataCadastro": ' || j_data(l.data_cadastro) || '}');
        END LOOP;

        add(v_json, CHR(10) || ']');
        RETURN v_json;
    END json_clinicas;

    FUNCTION exportar_colecao_json (
        p_colecao IN VARCHAR2
    ) RETURN CLOB IS
    BEGIN
        exigir(p_colecao, 'p_colecao');

        CASE UPPER(TRIM(p_colecao))
            WHEN 'TUTORES'      THEN RETURN json_tutores;
            WHEN 'VETERINARIOS' THEN RETURN json_veterinarios;
            WHEN 'CLINICAS'     THEN RETURN json_clinicas;
            ELSE
                RAISE_APPLICATION_ERROR(-20010,
                    'Colecao invalida: ' || p_colecao || '. Use TUTORES, VETERINARIOS ou CLINICAS.');
        END CASE;
    EXCEPTION
        WHEN OTHERS THEN
            relancar('exportar_colecao_json');
    END exportar_colecao_json;

    -- =================================================================
    -- Relatorio
    -- =================================================================

    PROCEDURE resumo_consultas (
        p_data_inicio IN  DATE DEFAULT NULL,
        p_data_fim    IN  DATE DEFAULT NULL,
        p_cursor      OUT SYS_REFCURSOR
    ) IS
    BEGIN
        IF p_data_inicio IS NOT NULL AND p_data_fim IS NOT NULL
           AND p_data_inicio > p_data_fim THEN
            RAISE_APPLICATION_ERROR(-20011, 'A data inicial e maior que a data final.');
        END IF;

        OPEN p_cursor FOR
            SELECT CASE
                       WHEN GROUPING(v.id) = 1            THEN 'TOTAL_GERAL'
                       WHEN GROUPING(c.tipo_consulta) = 1 THEN 'SUBTOTAL'
                       ELSE 'DETALHE'
                   END                    AS tipo_linha,
                   v.id                   AS veterinario_id,
                   v.nome                 AS veterinario,
                   c.tipo_consulta        AS tipo_consulta,
                   COUNT(*)               AS quantidade,
                   SUM(c.valor)           AS valor_total
              FROM tb_consultas c
              JOIN tb_veterinarios v ON v.id = c.veterinario_id
             WHERE c.status = 'REALIZADA'
               AND (p_data_inicio IS NULL OR c.data_prevista >= TRUNC(p_data_inicio))
               AND (p_data_fim    IS NULL OR c.data_prevista <  TRUNC(p_data_fim) + 1)
             GROUP BY GROUPING SETS ((v.id, v.nome, c.tipo_consulta), (v.id, v.nome), ())
             ORDER BY GROUPING(v.id), v.nome, v.id, GROUPING(c.tipo_consulta), c.tipo_consulta;
    EXCEPTION
        WHEN OTHERS THEN
            relancar('resumo_consultas');
    END resumo_consultas;

    -- =================================================================
    -- Auditoria
    -- =================================================================

    PROCEDURE definir_usuario_aplicacao (
        p_usuario IN VARCHAR2
    ) IS
    BEGIN
        DBMS_SESSION.SET_IDENTIFIER(SUBSTR(p_usuario, 1, 64));
    END definir_usuario_aplicacao;

    PROCEDURE registrar_auditoria (
        p_consulta_id IN NUMBER,
        p_operacao    IN VARCHAR2,
        p_anteriores  IN VARCHAR2,
        p_novos       IN VARCHAR2
    ) IS
    BEGIN
        INSERT INTO tb_auditoria_consultas (
            consulta_id, nome_usuario, usuario_aplicacao, tipo_operacao,
            data_hora_operacao, valores_anteriores, valores_novos
        ) VALUES (
            p_consulta_id,
            SYS_CONTEXT('USERENV', 'SESSION_USER'),
            SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'),
            p_operacao,
            SYSTIMESTAMP,
            SUBSTR(p_anteriores, 1, 4000),
            SUBSTR(p_novos, 1, 4000)
        );
    EXCEPTION
        -- Sem auditoria, a operacao nao deve ser gravada
        WHEN OTHERS THEN
            RAISE_APPLICATION_ERROR(-20012,
                'Falha ao auditar a consulta ' || p_consulta_id || ': ' || SQLERRM);
    END registrar_auditoria;

END pkg_clyvo;
/

CREATE OR REPLACE TRIGGER trg_auditoria_consultas
    AFTER INSERT OR UPDATE OR DELETE ON tb_consultas
    FOR EACH ROW
DECLARE
    v_operacao   VARCHAR2(10);
    v_anteriores VARCHAR2(4000);
    v_novos      VARCHAR2(4000);
BEGIN
    IF INSERTING THEN
        v_operacao := 'INSERT';
    ELSIF UPDATING THEN
        v_operacao := 'UPDATE';
    ELSE
        v_operacao := 'DELETE';
    END IF;

    IF NOT INSERTING THEN
        v_anteriores := SUBSTR(
               'responsavel_id=' || :OLD.responsavel_id
            || '; pet_id='         || :OLD.pet_id
            || '; clinica_id='     || :OLD.clinica_id
            || '; veterinario_id=' || :OLD.veterinario_id
            || '; data_prevista='  || TO_CHAR(:OLD.data_prevista, 'YYYY-MM-DD HH24:MI')
            || '; tipo='           || :OLD.tipo_consulta
            || '; status='         || :OLD.status
            || '; valor='          || TO_CHAR(:OLD.valor)
            || '; retorno='        || TO_CHAR(:OLD.retorno_previsto, 'YYYY-MM-DD HH24:MI')
            || '; diagnostico='    || :OLD.diagnostico, 1, 4000);
    END IF;

    IF NOT DELETING THEN
        v_novos := SUBSTR(
               'responsavel_id=' || :NEW.responsavel_id
            || '; pet_id='         || :NEW.pet_id
            || '; clinica_id='     || :NEW.clinica_id
            || '; veterinario_id=' || :NEW.veterinario_id
            || '; data_prevista='  || TO_CHAR(:NEW.data_prevista, 'YYYY-MM-DD HH24:MI')
            || '; tipo='           || :NEW.tipo_consulta
            || '; status='         || :NEW.status
            || '; valor='          || TO_CHAR(:NEW.valor)
            || '; retorno='        || TO_CHAR(:NEW.retorno_previsto, 'YYYY-MM-DD HH24:MI')
            || '; diagnostico='    || :NEW.diagnostico, 1, 4000);
    END IF;

    pkg_clyvo.registrar_auditoria(
        NVL(:NEW.id, :OLD.id), v_operacao, v_anteriores, v_novos
    );
END trg_auditoria_consultas;
/