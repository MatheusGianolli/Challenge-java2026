-- =====================================================================
-- V5 - Regras de integridade do modelo
--   1. UNIQUE: identificadores de negocio que nao podem se repetir
--   2. NOT NULL: campos essenciais (os mesmos exigidos pela API)
--   3. CHECK: regras de dominio simples
--
-- As constraints recebem nome para aparecerem claras nos modelos
-- (Data Modeler) e nas mensagens de erro tratadas pela API.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Unicidade
-- ---------------------------------------------------------------------
ALTER TABLE tb_tutores      ADD CONSTRAINT uk_tutores_documento   UNIQUE (numero_documento);
ALTER TABLE tb_veterinarios ADD CONSTRAINT uk_veterinarios_crmv   UNIQUE (crmv);
ALTER TABLE tb_clinicas     ADD CONSTRAINT uk_clinicas_cnpj       UNIQUE (cnpj);
-- Microchip e opcional; o UNIQUE do Oracle ignora valores nulos
ALTER TABLE tb_pets         ADD CONSTRAINT uk_pets_microchip      UNIQUE (microchip);

-- ---------------------------------------------------------------------
-- 2. Campos obrigatorios
-- ---------------------------------------------------------------------
ALTER TABLE tb_tutores MODIFY (
    nome             CONSTRAINT nn_tutores_nome      NOT NULL,
    numero_documento CONSTRAINT nn_tutores_documento NOT NULL,
    email            CONSTRAINT nn_tutores_email     NOT NULL,
    telefone         CONSTRAINT nn_tutores_telefone  NOT NULL,
    status           CONSTRAINT nn_tutores_status    NOT NULL
);

ALTER TABLE tb_veterinarios MODIFY (
    nome     CONSTRAINT nn_veterinarios_nome     NOT NULL,
    crmv     CONSTRAINT nn_veterinarios_crmv     NOT NULL,
    email    CONSTRAINT nn_veterinarios_email    NOT NULL,
    telefone CONSTRAINT nn_veterinarios_telefone NOT NULL,
    status   CONSTRAINT nn_veterinarios_status   NOT NULL
);

ALTER TABLE tb_clinicas MODIFY (
    nome     CONSTRAINT nn_clinicas_nome     NOT NULL,
    cnpj     CONSTRAINT nn_clinicas_cnpj     NOT NULL,
    email    CONSTRAINT nn_clinicas_email    NOT NULL,
    telefone CONSTRAINT nn_clinicas_telefone NOT NULL,
    status   CONSTRAINT nn_clinicas_status   NOT NULL
);

ALTER TABLE tb_pets MODIFY (
    nome    CONSTRAINT nn_pets_nome    NOT NULL,
    especie CONSTRAINT nn_pets_especie NOT NULL,
    sexo    CONSTRAINT nn_pets_sexo    NOT NULL
);

ALTER TABLE tb_consultas MODIFY (
    tipo_consulta CONSTRAINT nn_consultas_tipo   NOT NULL,
    status        CONSTRAINT nn_consultas_status NOT NULL
);

-- ---------------------------------------------------------------------
-- 3. Regras de dominio
-- ---------------------------------------------------------------------
ALTER TABLE tb_pets ADD CONSTRAINT ck_pets_peso
    CHECK (peso IS NULL OR peso >= 0) ENABLE NOVALIDATE;

ALTER TABLE tb_pets ADD CONSTRAINT ck_pets_altura
    CHECK (altura IS NULL OR altura >= 0) ENABLE NOVALIDATE;

-- O retorno nunca pode ser marcado para antes da propria consulta
ALTER TABLE tb_consultas ADD CONSTRAINT ck_consultas_retorno
    CHECK (retorno_previsto IS NULL OR retorno_previsto > data_prevista) ENABLE NOVALIDATE;

-- Documentacao no dicionario de dados
COMMENT ON COLUMN tb_tutores.numero_documento IS 'CPF ou CNPJ do responsavel (unico)';
COMMENT ON COLUMN tb_pets.microchip           IS 'Numero do microchip (unico quando informado)';
COMMENT ON COLUMN tb_consultas.responsavel_id IS 'FK para TB_TUTORES. Redundante com TB_PETS.RESPONSAVEL_ID para agilizar a listagem; a consistencia e garantida por PKG_CLYVO.AGENDAR_CONSULTA';