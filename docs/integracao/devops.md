# DevOps Tools & Cloud Computing

**Responsável:** Larissa Juvenal de Magalhães (RM566457)

## Como a disciplina se conecta ao projeto

- **Banco em nuvem:** a aplicação usa o Oracle da FIAP, o mesmo schema da disciplina de Database. O Flyway cria e versiona o banco quando a aplicação sobe, então qualquer ambiente novo nasce com a estrutura correta.
- **Container:** o `Dockerfile` da raiz faz o build com Maven em duas etapas e executa com JRE 17 e usuário sem privilégios. É essa imagem que roda na versão online da aplicação.
- **Credenciais fora do código:** usuário e senha do banco vêm de variáveis de ambiente (`DB_USERNAME`, `DB_PASSWORD`), prontas para serem configuradas como secrets de pipeline.
- **Azure DevOps:** o projeto **clyvo-petcare-devops** reúne os integrantes do grupo e é o espaço da disciplina para as pipelines de CI/CD da solução.

## Evidências

- `Dockerfile` e `.dockerignore` na raiz do repositório
- Aplicação publicada: https://clyvo-petcare.onrender.com
- Projeto no Azure DevOps: https://dev.azure.com/RM564995/clyvo-petcare-devops
