# Compliance, Quality Assurance & Tests

**Responsável:** Júlia Kauane Menezes Farias (RM565568)

## Como a disciplina se conecta ao projeto

**Testes automatizados no repositório:** `src/test/java/br/com/challenge/util/StatusCadastroTest.java` testa a regra de status dos cadastros (valores aceitos, normalização, status vazio e inválido), sem depender do banco.

**Regras de negócio que a API garante** e que servem de base para o plano de testes:

| Caso | Resultado esperado |
|---|---|
| Agendar com o veterinário já ocupado no mesmo horário | 409 "O veterinario ja possui consulta neste horario." |
| Agendar com data no passado | 409 "A data prevista deve ser no futuro." |
| Veterinário com a agenda do dia cheia (8 consultas) | indisponível em `GET /api/consultas/disponibilidade` |
| Cadastrar veterinário com CRMV repetido | 409 "Já existe um veterinário com este CRMV" |
| Perfil `veterinario` tentando excluir registros | 403 |
| Campo obrigatório em branco | 400 com a mensagem do campo |

## Evidências

- Testes unitários: `src/test/java/br/com/challenge/util/StatusCadastroTest.java`
- Endpoints testáveis pelo Swagger (`/swagger-ui/index.html`) e pela coleção em `docs/api/`
