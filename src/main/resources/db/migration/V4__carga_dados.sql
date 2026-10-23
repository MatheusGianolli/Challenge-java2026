-- =====================================================================
-- V4 - Carga inicial de dados (minimo de 5 registros por tabela)
-- As FKs sao resolvidas por subconsulta em campos de negocio (e-mail,
-- CRMV, CNPJ, microchip) em vez de IDs fixos: como os IDs sao IDENTITY,
-- o script funciona mesmo que o banco ja tenha registros de testes.
-- MIN(id) garante uma unica linha; se o registro nao existir, retorna
-- NULL e a FK NOT NULL falha de forma explicita.
-- =====================================================================

-- TB_TUTORES
INSERT INTO tb_tutores (nome, tipo_documento, numero_documento, data_nascimento, email, telefone,
    tipo_responsavel, preferencia_contato, recebe_notificacao, autoriza_monitoramento,
    logradouro, numero_endereco, bairro, cidade, estado, cep, status, data_cadastro)
VALUES ('Ana Paula Ferreira', 'CPF', '123.456.789-01', DATE '1988-04-12', 'ana.ferreira@email.com', '(11) 99811-2233',
    'TUTOR', 'WHATSAPP', 'S', 'S',
    'Rua das Palmeiras', '120', 'Vila Mariana', 'São Paulo', 'SP', '04101-000', 'ATIVO', SYSTIMESTAMP);

INSERT INTO tb_tutores (nome, tipo_documento, numero_documento, data_nascimento, email, telefone,
    tipo_responsavel, preferencia_contato, recebe_notificacao, autoriza_monitoramento,
    logradouro, numero_endereco, bairro, cidade, estado, cep, status, data_cadastro)
VALUES ('Bruno Carvalho Lima', 'CPF', '234.567.890-12', DATE '1992-09-03', 'bruno.lima@email.com', '(11) 99722-3344',
    'TUTOR', 'EMAIL', 'S', 'S',
    'Av. Paulista', '1500', 'Bela Vista', 'São Paulo', 'SP', '01310-100', 'ATIVO', SYSTIMESTAMP);

INSERT INTO tb_tutores (nome, tipo_documento, numero_documento, data_nascimento, email, telefone,
    tipo_responsavel, preferencia_contato, recebe_notificacao, autoriza_monitoramento,
    logradouro, numero_endereco, bairro, cidade, estado, cep, status, data_cadastro)
VALUES ('Carla Menezes Souza', 'CPF', '345.678.901-23', DATE '1979-01-25', 'carla.souza@email.com', '(11) 99633-4455',
    'TUTOR', 'TELEFONE', 'S', 'S',
    'Rua Augusta', '780', 'Consolação', 'São Paulo', 'SP', '01305-000', 'ATIVO', SYSTIMESTAMP);

INSERT INTO tb_tutores (nome, tipo_documento, numero_documento, data_nascimento, email, telefone,
    tipo_responsavel, preferencia_contato, recebe_notificacao, autoriza_monitoramento,
    logradouro, numero_endereco, bairro, cidade, estado, cep, status, data_cadastro)
VALUES ('Diego Almeida Rocha', 'CPF', '456.789.012-34', DATE '1995-06-18', 'diego.rocha@email.com', '(11) 99544-5566',
    'TUTOR', 'WHATSAPP', 'S', 'S',
    'Rua Harmonia', '33', 'Vila Madalena', 'São Paulo', 'SP', '05435-000', 'ATIVO', SYSTIMESTAMP);

INSERT INTO tb_tutores (nome, tipo_documento, numero_documento, data_nascimento, email, telefone,
    tipo_responsavel, preferencia_contato, recebe_notificacao, autoriza_monitoramento,
    logradouro, numero_endereco, bairro, cidade, estado, cep, status, data_cadastro)
VALUES ('Instituto Patas Unidas', 'CNPJ', '12.345.678/0001-90', NULL, 'contato@patasunidas.org', '(11) 3456-7788',
    'ONG', 'EMAIL', 'S', 'S',
    'Rua do Acolhimento', '900', 'Mooca', 'São Paulo', 'SP', '03101-000', 'ATIVO', SYSTIMESTAMP);

-- TB_CLINICAS
INSERT INTO tb_clinicas (nome, cnpj, email, telefone, logradouro, numero_endereco, bairro,
    cidade, estado, cep, status, data_cadastro, tipo_estabelecimento, especialidade_principal,
    atendimento_24h, atende_emergencia)
VALUES ('Clínica Vet Vida', '11.222.333/0001-01', 'contato@vetvida.com.br', '(11) 3222-1000', 'Rua Domingos de Morais', '1200', 'Vila Mariana',
    'São Paulo', 'SP', '04010-100', 'ATIVO', SYSTIMESTAMP, 'CLINICA VETERINARIA', 'Clínica Geral',
    'N', 'N');

INSERT INTO tb_clinicas (nome, cnpj, email, telefone, logradouro, numero_endereco, bairro,
    cidade, estado, cep, status, data_cadastro, tipo_estabelecimento, especialidade_principal,
    atendimento_24h, atende_emergencia)
VALUES ('Hospital Veterinário Pet Center', '22.333.444/0001-02', 'atendimento@petcenter.com.br', '(11) 3333-2000', 'Av. Rebouças', '3000', 'Pinheiros',
    'São Paulo', 'SP', '05402-600', 'ATIVO', SYSTIMESTAMP, 'HOSPITAL VETERINARIO', 'Emergência e Cirurgia',
    'S', 'S');

INSERT INTO tb_clinicas (nome, cnpj, email, telefone, logradouro, numero_endereco, bairro,
    cidade, estado, cep, status, data_cadastro, tipo_estabelecimento, especialidade_principal,
    atendimento_24h, atende_emergencia)
VALUES ('Clínica Amigo Fiel', '33.444.555/0001-03', 'contato@amigofiel.com.br', '(11) 3444-3000', 'Rua Tuiuti', '450', 'Tatuapé',
    'São Paulo', 'SP', '03081-000', 'ATIVO', SYSTIMESTAMP, 'CLINICA VETERINARIA', 'Dermatologia',
    'N', 'N');

INSERT INTO tb_clinicas (nome, cnpj, email, telefone, logradouro, numero_endereco, bairro,
    cidade, estado, cep, status, data_cadastro, tipo_estabelecimento, especialidade_principal,
    atendimento_24h, atende_emergencia)
VALUES ('Centro Veterinário Bicho Feliz', '44.555.666/0001-04', 'ola@bichofeliz.com.br', '(11) 3555-4000', 'Av. Santo Amaro', '2100', 'Brooklin',
    'São Paulo', 'SP', '04556-100', 'ATIVO', SYSTIMESTAMP, 'CLINICA VETERINARIA', 'Cardiologia',
    'N', 'S');

INSERT INTO tb_clinicas (nome, cnpj, email, telefone, logradouro, numero_endereco, bairro,
    cidade, estado, cep, status, data_cadastro, tipo_estabelecimento, especialidade_principal,
    atendimento_24h, atende_emergencia)
VALUES ('Pronto Socorro Vet 24h', '55.666.777/0001-05', 'ps@vet24h.com.br', '(11) 3666-5000', 'Av. Brigadeiro Faria Lima', '800', 'Itaim Bibi',
    'São Paulo', 'SP', '01452-000', 'ATIVO', SYSTIMESTAMP, 'HOSPITAL VETERINARIO', 'Emergência',
    'S', 'S');

-- TB_VETERINARIOS
INSERT INTO tb_veterinarios (nome, crmv, especialidade, tipo_atuacao, email, telefone, status, data_cadastro)
VALUES ('Dr. Rafael Nogueira', 'CRMV-SP 40101', 'Clínica Geral', 'CLINICA', 'rafael.nogueira@vet.com.br', '(11) 98100-1001', 'ATIVO', SYSTIMESTAMP);

INSERT INTO tb_veterinarios (nome, crmv, especialidade, tipo_atuacao, email, telefone, status, data_cadastro)
VALUES ('Dra. Juliana Prado', 'CRMV-SP 40202', 'Dermatologia', 'HIBRIDO', 'juliana.prado@vet.com.br', '(11) 98200-2002', 'ATIVO', SYSTIMESTAMP);

INSERT INTO tb_veterinarios (nome, crmv, especialidade, tipo_atuacao, email, telefone, status, data_cadastro)
VALUES ('Dr. Marcelo Vieira', 'CRMV-SP 40303', 'Cirurgia', 'CLINICA', 'marcelo.vieira@vet.com.br', '(11) 98300-3003', 'ATIVO', SYSTIMESTAMP);

INSERT INTO tb_veterinarios (nome, crmv, especialidade, tipo_atuacao, email, telefone, status, data_cadastro)
VALUES ('Dra. Patricia Gomes', 'CRMV-SP 40404', 'Cardiologia', 'AUTONOMO', 'patricia.gomes@vet.com.br', '(11) 98400-4004', 'ATIVO', SYSTIMESTAMP);

INSERT INTO tb_veterinarios (nome, crmv, especialidade, tipo_atuacao, email, telefone, status, data_cadastro)
VALUES ('Dr. Thiago Barros', 'CRMV-SP 40505', 'Clínica Geral', 'HIBRIDO', 'thiago.barros@vet.com.br', '(11) 98500-5005', 'ATIVO', SYSTIMESTAMP);

-- TB_PETS
INSERT INTO tb_pets (responsavel_id, nome, especie, raca, data_nascimento, peso, altura, sexo,
    porte, status_saude, alergias, castrado, cor_pelagem, microchip, data_cadastro)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'ana.ferreira@email.com'),
    'Rex', 'Cachorro', 'Labrador', DATE '2021-03-15', 32.5, 0.58, 'M',
    'GRANDE', 'SAUDAVEL', NULL, 'S', 'Caramelo', '985112000000101', SYSTIMESTAMP);

INSERT INTO tb_pets (responsavel_id, nome, especie, raca, data_nascimento, peso, altura, sexo,
    porte, status_saude, alergias, castrado, cor_pelagem, microchip, data_cadastro)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'bruno.lima@email.com'),
    'Mimi', 'Gato', 'Siamês', DATE '2020-07-22', 4.2, 0.25, 'F',
    'PEQUENO', 'SAUDAVEL', 'Frango', 'S', 'Creme e marrom', '985112000000102', SYSTIMESTAMP);

INSERT INTO tb_pets (responsavel_id, nome, especie, raca, data_nascimento, peso, altura, sexo,
    porte, status_saude, alergias, castrado, cor_pelagem, microchip, data_cadastro)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'carla.souza@email.com'),
    'Thor', 'Cachorro', 'Bulldog Francês', DATE '2019-11-05', 12.8, 0.33, 'M',
    'MEDIO', 'EM TRATAMENTO', 'Dipirona', 'N', 'Tigrado', '985112000000103', SYSTIMESTAMP);

INSERT INTO tb_pets (responsavel_id, nome, especie, raca, data_nascimento, peso, altura, sexo,
    porte, status_saude, alergias, castrado, cor_pelagem, microchip, data_cadastro)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'diego.rocha@email.com'),
    'Nina', 'Gato', 'Persa', DATE '2022-01-30', 3.9, 0.24, 'F',
    'PEQUENO', 'SAUDAVEL', NULL, 'S', 'Branca', '985112000000104', SYSTIMESTAMP);

INSERT INTO tb_pets (responsavel_id, nome, especie, raca, data_nascimento, peso, altura, sexo,
    porte, status_saude, alergias, castrado, cor_pelagem, microchip, data_cadastro)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'contato@patasunidas.org'),
    'Bidu', 'Cachorro', 'SRD', DATE '2018-05-10', 18.0, 0.45, 'M',
    'MEDIO', 'EM OBSERVACAO', NULL, 'S', 'Preto', '985112000000105', SYSTIMESTAMP);

INSERT INTO tb_pets (responsavel_id, nome, especie, raca, data_nascimento, peso, altura, sexo,
    porte, status_saude, alergias, castrado, cor_pelagem, microchip, data_cadastro)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'ana.ferreira@email.com'),
    'Luna', 'Gato', 'Angorá', DATE '2021-09-18', 4.5, 0.26, 'F',
    'PEQUENO', 'SAUDAVEL', NULL, 'S', 'Cinza', '985112000000106', SYSTIMESTAMP);

-- TB_CONSULTAS (fato)
INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'ana.ferreira@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000101'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '11.222.333/0001-01'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40101'),
    SYSTIMESTAMP, TIMESTAMP '2026-08-03 09:00:00', NULL, 'Rotina',
    'Check-up anual', 'Animal saudável, vacinas em dia', 'REALIZADA', 150.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'bruno.lima@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000102'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '11.222.333/0001-01'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40101'),
    SYSTIMESTAMP, TIMESTAMP '2026-08-03 10:00:00', NULL, 'Vacinação',
    'Reforço da vacina quádrupla felina', 'Vacina aplicada sem intercorrências', 'REALIZADA', 90.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'carla.souza@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000103'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '33.444.555/0001-03'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40202'),
    SYSTIMESTAMP, TIMESTAMP '2026-08-05 14:00:00', TIMESTAMP '2026-08-19 14:00:00', 'Rotina',
    'Coceira intensa e queda de pelo', 'Dermatite alérgica, iniciado tratamento tópico', 'REALIZADA', 180.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'carla.souza@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000103'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '33.444.555/0001-03'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40202'),
    SYSTIMESTAMP, TIMESTAMP '2026-08-19 14:00:00', NULL, 'Retorno',
    'Reavaliação da dermatite', 'Melhora significativa, manter tratamento por 15 dias', 'REALIZADA', 80.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'contato@patasunidas.org'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000105'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '22.333.444/0001-02'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40303'),
    SYSTIMESTAMP, TIMESTAMP '2026-08-10 08:00:00', TIMESTAMP '2026-08-24 08:00:00', 'Cirurgia',
    'Nódulo na pata traseira', 'Exérese do nódulo realizada, material enviado para biópsia', 'REALIZADA', 1200.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'contato@patasunidas.org'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000105'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '22.333.444/0001-02'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40303'),
    SYSTIMESTAMP, TIMESTAMP '2026-08-24 08:30:00', NULL, 'Retorno',
    'Retirada de pontos', 'Cicatrização adequada', 'REALIZADA', 80.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'diego.rocha@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000104'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '44.555.666/0001-04'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40404'),
    SYSTIMESTAMP, TIMESTAMP '2026-08-12 16:00:00', NULL, 'Rotina',
    'Cansaço ao brincar', 'Sopro cardíaco leve, solicitado ecocardiograma', 'REALIZADA', 220.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'ana.ferreira@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000106'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '55.666.777/0001-05'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40505'),
    SYSTIMESTAMP, TIMESTAMP '2026-08-15 23:30:00', NULL, 'Emergência',
    'Vômitos repetidos', 'Gastrite aguda, medicada e liberada', 'REALIZADA', 400.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'ana.ferreira@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000101'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '55.666.777/0001-05'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40505'),
    SYSTIMESTAMP, TIMESTAMP '2026-09-02 02:15:00', NULL, 'Emergência',
    'Ingestão de corpo estranho', 'Corpo estranho removido por endoscopia', 'REALIZADA', 950.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'bruno.lima@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000102'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '11.222.333/0001-01'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40101'),
    SYSTIMESTAMP, TIMESTAMP '2026-09-10 11:00:00', NULL, 'Rotina',
    'Perda de apetite', NULL, 'CANCELADA', 150.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'ana.ferreira@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000106'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '11.222.333/0001-01'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40101'),
    SYSTIMESTAMP, TIMESTAMP '2026-10-20 09:00:00', NULL, 'Vacinação',
    'Vacina antirrábica anual', NULL, 'AGENDADA', 90.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'diego.rocha@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000104'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '44.555.666/0001-04'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40404'),
    SYSTIMESTAMP, TIMESTAMP '2026-10-22 15:00:00', NULL, 'Retorno',
    'Resultado do ecocardiograma', NULL, 'AGENDADA', 120.00);

INSERT INTO tb_consultas (responsavel_id, pet_id, clinica_id, veterinario_id, data_criacao,
    data_prevista, retorno_previsto, tipo_consulta, descricao_sintomas, diagnostico, status, valor)
VALUES ((SELECT MIN(id) FROM tb_tutores WHERE email = 'carla.souza@email.com'),
    (SELECT MIN(id) FROM tb_pets WHERE microchip = '985112000000103'),
    (SELECT MIN(id) FROM tb_clinicas WHERE cnpj = '33.444.555/0001-03'),
    (SELECT MIN(id) FROM tb_veterinarios WHERE crmv = 'CRMV-SP 40202'),
    SYSTIMESTAMP, TIMESTAMP '2026-11-05 10:00:00', NULL, 'Retorno',
    'Acompanhamento da dermatite', NULL, 'AGENDADA', 80.00);