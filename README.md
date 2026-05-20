🐾 Challenge: API RESTful para Sistema Veterinário

API RESTful desenvolvida em Java com Spring Boot para o gerenciamento de clínicas veterinárias, tutores, pets e consultas. O projeto contempla operações completas de CRUD, persistência de dados relacional, paginação e documentação automatizada, seguindo as diretrizes da Sprint.

---

##  Integrantes do Grupo
* **Matheus Gianolli** — RM: 565258
* **Enzo Xavier Coelho** — RM: 563379
* **Gustavo Ribeiro Permagnani** — RM: 564995
* **Larissa Juvenal de Magalhaes** — RM: 566457
* **Julia Menezes** — RM: 565568

```text
Estrutura de pastas
-------------------

challenge-api/
├── documentos/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/challenge/
│   │   │       ├── config/
│   │   │       │   └── OpenApiConfig.java
│   │   │       ├── controllers/
│   │   │       │   ├── ClinicaController.java
│   │   │       │   ├── ConsultaController.java
│   │   │       │   ├── PetController.java
│   │   │       │   ├── TutorController.java
│   │   │       │   └── VeterinarioController.java
│   │   │       ├── dtos/
│   │   │       │   ├── ClinicaDTO.java
│   │   │       │   ├── ConsultaDTO.java
│   │   │       │   ├── PetDTO.java
│   │   │       │   ├── TutorDTO.java
│   │   │       │   └── VeterinarioDTO.java
│   │   │       ├── exceptions/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── ResourceNotFoundException.java
│   │   │       ├── models/
│   │   │       │   ├── Clinica.java
│   │   │       │   ├── Consulta.java
│   │   │       │   ├── Pet.java
│   │   │       │   ├── Tutor.java
│   │   │       │   └── Veterinario.java
│   │   │       ├── repositories/
│   │   │       │   ├── ClinicaRepository.java
│   │   │       │   ├── ConsultaRepository.java
│   │   │       │   ├── PetRepository.java
│   │   │       │   ├── TutorRepository.java
│   │   │       │   └── VeterinarioRepository.java
│   │   │       ├── services/
│   │   │       │   ├── ClinicaService.java
│   │   │       │   ├── ConsultaService.java
│   │   │       │   ├── PetService.java
│   │   │       │   ├── TutorService.java
│   │   │       │   └── VeterinarioService.java
│   │   │       └── ChallengeApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

---

##  Tecnologias Utilizadas
* **Linguagem:** Java 17
* **Framework:** Spring Boot 3.2.5
* **Banco de Dados:** H2 Database (Memória para testes) e Oracle (Oficial)
* **Validação:** Jakarta Bean Validation
* **Documentação:** Swagger (Springdoc OpenAPI)
* **Testes:** Postman

>  **Nota de Implementação:** > Optou-se por aplicar esses recursos avançados (Cache e Query Methods) exclusivamente na entidade `Veterinario` como uma "Prova de Conceito" 

---



 Como Executar o Projeto
1. Clone o repositório na sua máquina:
   `https://github.com/MatheusGianolli/Challenge-java2026`
2. Importe o projeto na sua IDE (recomendado: IntelliJ IDEA).
3. Aguarde a sincronização das dependências do Maven.
4. Execute a classe principal `ChallengeApplication.java`.
5. Execute no botão verde logo ao lado de "	public static void main(String[] args) {"
		SpringApplication.run(ChallengeApplication.class, args);
	}
6. A API estará disponível e rodando na porta local: `http://localhost:8080`.

##  Documentação da API (Swagger)
A documentação interativa gerada automaticamente pelo Swagger contendo todos os endpoints, parâmetros e esquemas JSON pode ser acessada com a aplicação rodando no link:
 `http://localhost:8080/swagger-ui/index.html`

**Print do Swagger em funcionamento:**
![Print do Swagger](documentos/print_swagger.png)



Na imagem abaixo, é possível observar a organização clara das rotas da API, separadas por domínios (entidades) e com a sinalização visual de todos os métodos HTTP (GET, POST, PUT e DELETE) implementados nos CRUDs:

![Interface do Swagger UI](./documentos/swagger_api2.png)
<br>
![Interface do Swagger UI - Tutores e Consultas](./documentos/swagger_api3.png)
---

##   Testes de Requisição (Postman)
Para a validação das regras de negócio, todos os endpoints foram testados. O arquivo `.json` completo exportado da Collection do Postman encontra-se disponível na pasta `documentos/` deste repositório para importação e avaliação.





### 1. Cadastro de Tutor (POST)
![Print Cadastro Tutor](./documentos/01_post_tutor.png)

### 2. Listagem de Tutores com Paginação (GET)
![Print GET Tutores](./documentos/02_get_tutores.png)

### 3. Cadastro de Clínica (POST)!
![Print Cadastro Clinica](./documentos/03_post_clinica.png)

### 4. Cadastro de Veterinário (POST)
![Print Cadastro Veterinario](./documentos/04_post_veterinario.png)

### 5. Cadastro de Pet (POST)
![Print Cadastro Pet](./documentos/05_post_pet.png)

### 6. Agendamento de Consulta (POST)
![Print Cadastro Consulta](./documentos/06_post_consulta.png)

#### 7. Lançamento de Diagnóstico de Consulta (PUT)
Demonstração da atualização do prontuário médico. A requisição insere o diagnóstico final e o sistema altera automaticamente o status da consulta para `REALIZADA`.
![Lançamento de Diagnóstico](./documentos/07_put_consulta.png)
#### 8. Cancelamento de Consulta (DELETE)
Demonstração da exclusão lógica (Soft Delete). O registro não é apagado do banco de dados para manter o histórico médico íntegro, mas seu status é atualizado para `CANCELADA`.
![Cancelamento de Consulta](./documentos/08_delete_consulta.png)

#### 9. Atualização de Dados do Pet (PUT)
Demonstração da atualização das informações de um paciente (Pet). A requisição envia um novo payload JSON e o sistema sobrescreve os dados no banco de dados.
![Atualização do Pet](./documentos/09_put_pet.png)

#### 10. Exclusão de Pet (DELETE)
Demonstração da remoção de um Pet do sistema. O endpoint recebe o ID via parâmetro na URL e deleta o registro correspondente do banco de dados.
![Exclusão de Pet](./documentos/10_delete_pet.png)



---
---

##  Tratamento de Erros e Exceções (Validação Funcional)
Conforme solicitado nos requisitos técnicos, a aplicação utiliza um mapeador global de exceções (`@RestControllerAdvice`). Quando um campo obrigatório viola as regras do Bean Validation (ex: enviar um campo obrigatório em branco), a API intercepta a requisição e retorna o Status **400 Bad Request** com os detalhes amigáveis do erro.

**Prova do Tratamento de Exceções de Validação:**
-Como vemos no print abaixo caso o usuario esqueça de preencher um campo obrigatorio(no caso abaixo o nome ) graças a nossa excepition ele ira retornar um 404 bad request e sinalizara para o usuario que o campo é obrigatorio
![Print Tratamento de Erros](./documentos/07_tratamento_erros.png)

---

##  Modelagem de Dados e Arquitetura
### Diagrama de Classes
Representação da arquitetura orientada a objetos das entidades do sistema mapeadas no Java:
![Diagrama de Classes](documentos/diagrama_classes.png)

---

##  Divisão de Tarefas e Cronograma
A gestão ágil do projeto e a divisão técnica das responsabilidades desenvolvidas por cada integrante do grupo durante esta Sprint estão documentadas no arquivo em anexo.

* **Consulte o arquivo:** `cronograma.pdf` (localizado na pasta `documentos/`).
