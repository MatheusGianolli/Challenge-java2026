🐾 Challenge: API RESTful para Sistema Veterinário

API RESTful desenvolvida em Java com Spring Boot para o gerenciamento de clínicas veterinárias, tutores, pets e consultas. O projeto contempla operações completas de CRUD, persistência de dados relacional, paginação e documentação automatizada, seguindo as diretrizes da Sprint.

---

## 👥 Integrantes do Grupo
* **Matheus Gianolli** — RM: [Seu RM]
* **Enzo Xavier Coelho** — RM: [RM do Enzo]
* **Gustavo Ribeiro Permagnani** — RM: [RM do Gustavo]

---

## 🛠️ Tecnologias Utilizadas
* **Linguagem:** Java 17
* **Framework:** Spring Boot 3.2.5
* **Banco de Dados:** H2 Database (Memória para testes) e Oracle (Oficial)
* **Validação:** Jakarta Bean Validation
* **Documentação:** Swagger (Springdoc OpenAPI)
* **Testes:** Postman

---



⚙️ Como Executar o Projeto
1. Clone o repositório na sua máquina:
   ``
2. Importe o projeto na sua IDE (recomendado: IntelliJ IDEA).
3. Aguarde a sincronização das dependências do Maven.
4. Execute a classe principal `ChallengeApplication.java`.
5. A API estará disponível e rodando na porta local: `http://localhost:8080`.

📖 Documentação da API (Swagger)
A documentação interativa gerada automaticamente pelo Swagger contendo todos os endpoints, parâmetros e esquemas JSON pode ser acessada com a aplicação rodando no link:
## 📖 Documentação da API (Swagger)
A documentação interativa gerada automaticamente pelo Swagger contendo todos os endpoints, parâmetros e esquemas JSON pode ser acessada com a aplicação rodando no link:
👉 `http://localhost:8080/swagger-ui/index.html`

**Print do Swagger em funcionamento:**
![Print do Swagger](documentos/print_swagger.png)

---

## 🧪 Testes de Requisição (Postman)
Para a validação das regras de negócio, todos os endpoints foram testados. O arquivo `.json` completo exportado da Collection do Postman encontra-se disponível na pasta `documentos/` deste repositório para importação e avaliação.

**Provas de Funcionamento das Rotas (Prints):**

### 1. Cadastro de Tutor (POST)
![Print Cadastro Tutor](documentos/01_post_tutor.png)

### 2. Listagem de Tutores com Paginação (GET)
![Print GET Tutores](documentos/02_get_tutores.png)

### 3. Cadastro de Clínica (POST)
![Print Cadastro Clinica](documentos/03_post_clinica.png)

### 4. Cadastro de Veterinário (POST)
![Print Cadastro Veterinario](documentos/04_post_veterinario.png)

### 5. Cadastro de Pet (POST)
![Print Cadastro Pet](documentos/05_post_pet.png)

### 6. Agendamento de Consulta (POST)
![Print Cadastro Consulta](documentos/06_post_consulta.png)

---

## 🗄️ Modelagem de Dados e Arquitetura

### Diagrama Entidade-Relacionamento (DER)
Abaixo, a modelagem do banco de dados relacional oficial estruturado para o Oracle, garantindo a integridade por meio de PKs e FKs:
![Diagrama DER](documentos/diagrama_der.png)

### Diagrama de Classes
Representação da arquitetura orientada a objetos das entidades do sistema mapeadas no Java:
![Diagrama de Classes](documentos/diagrama_classes.png)

---

## 📅 Divisão de Tarefas e Cronograma
A gestão ágil do projeto e a divisão técnica das responsabilidades desenvolvidas por cada integrante do grupo durante esta Sprint estão documentadas no arquivo em anexo.

* **Consulte o arquivo:** `cronograma.pdf` (localizado na pasta `documentos/`).
