# Mobile Application Development

**Responsável:** Matheus Gianolli (RM565258)

## Como a disciplina se conecta ao projeto

O aplicativo mobile é pensado como a porta de entrada do **tutor**: acompanhar os pets, as consultas e os retornos recomendados pelo veterinário.

A API Java já expõe os endpoints que um app precisa, com autenticação HTTP Basic e dados reais do banco Oracle:

| Necessidade do app | Endpoint da API |
|---|---|
| Login | `GET /api/me` |
| Pets do tutor (CRUD) | `GET/POST/PUT/DELETE /api/pets` |
| Saúde do pet (prontuário) | `GET /api/pets/{id}/historico` |
| Consultas | `GET /api/consultas` · `POST /api/consultas` |
| Lembretes de retorno | `GET /api/consultas/retornos?dias=30` |

Os endpoints estão documentados na coleção [`docs/api/clyvo-mobile.postman_collection.json`](../api/clyvo-mobile.postman_collection.json), que pode ser importada no Postman ou no Insomnia e aponta para a aplicação publicada.

## Evidências

- Coleção da API: `docs/api/clyvo-mobile.postman_collection.json`
- Swagger da API: `/swagger-ui/index.html`
