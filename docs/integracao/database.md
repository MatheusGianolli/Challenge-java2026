# Mastering Relational and Non-Relational Database

**Responsável:** Gustavo Ribeiro Permagnani (RM564995)

## Como a disciplina aparece no projeto

O banco Oracle usado pela aplicação Java é o próprio banco da disciplina: as regras de negócio do agendamento, o relatório de faturamento, a auditoria e a exportação para NoSQL ficam no banco, empacotadas no package `pkg_clyvo`, e são chamadas pela API.

| Item da disciplina | Onde está | Como é usado pela aplicação |
|---|---|---|
| Modelagem relacional | `database/entrega/2TDSPV_2026_Proj_BD.pdf` | Mesmo modelo das entidades JPA (tabelas `tb_*`) |
| Versionamento do banco | `src/main/resources/db/migration` (V1 a V5) | O Flyway cria e atualiza o banco ao iniciar a API |
| Package com procedures e funções | `R__pkg_clyvo.sql` | `PkgClyvoRepository` chama as rotinas via `CallableStatement` |
| Procedure de agendamento | `pkg_clyvo.agendar_consulta` | `POST /api/consultas` |
| Função de regra de negócio | `pkg_clyvo.valida_limite_agenda` | `GET /api/consultas/disponibilidade` |
| Procedure de relatório (soma manual) | `pkg_clyvo.resumo_consultas` | `GET /api/relatorios/resumo-consultas` |
| Função JSON manual | `pkg_clyvo.exportar_colecao_json` | `GET /api/relatorios/exportacao/{colecao}` |
| Trigger de auditoria | `trg_auditoria_consultas` | Registra toda alteração de consulta com o usuário logado na API |
| MongoDB | `database/mongodb/` e `database/json/` | Coleções `tutores` (pets e consultas embutidos), `veterinarios` e `clinicas` |

## Evidências

- Script completo com demonstração das rotinas e exceções: `database/entrega/2TDSPV_2026_CodigoSql_Clyvo.sql`
- Consultas de verificação no MongoDB: `database/mongodb/03_consultas.js` (o faturamento calculado no Mongo é igual ao do Oracle)
- Vídeo da disciplina: _link_
