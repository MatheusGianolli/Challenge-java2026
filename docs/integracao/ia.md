# Disruptive Architectures: IoT, IoB & Generative AI

**Responsável:** Enzo Xavier Coelho (RM563379)

## Como a disciplina se conecta ao projeto

A proposta é usar IA sobre o **histórico de saúde do pet** que o Clyvo centraliza. A API Java já entrega o prontuário consolidado em `GET /api/pets/{id}/historico` (perfil do animal, alergias, consultas, diagnósticos e próximo retorno), que é a entrada da IA para gerar:

- um resumo do prontuário para o veterinário;
- os pontos de atenção (alergias, tratamento em andamento, retorno atrasado ou próximo);
- uma mensagem de lembrete para o tutor.

O fluxo, o prompt e um exemplo de entrada e saída estão descritos em [`docs/ia/contexto_prontuario.md`](../ia/contexto_prontuario.md).

## Evidências

- Proposta de integração: `docs/ia/contexto_prontuario.md`
- Endpoint de origem dos dados: `GET /api/pets/{id}/historico` (Swagger)
