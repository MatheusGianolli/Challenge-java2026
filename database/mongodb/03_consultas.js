// =====================================================================
// 03_consultas.js — Consultas de exemplo sobre a estrutura NoSQL
// Executar com:  mongosh "<uri>" --file 03_consultas.js
// =====================================================================

db = db.getSiblingDB("clyvo");

print("\n1. Ficha completa de um tutor (tutor + pets + histórico em uma leitura)");
printjson(db.tutores.findOne({ "contato.email": "ana.ferreira@email.com" }));

print("\n2. Pets com alergia registrada");
db.tutores.aggregate([
  { $unwind: "$pets" },
  { $match: { "pets.alergias": { $ne: null } } },
  { $project: { _id: 0, tutor: "$nome", pet: "$pets.nome", alergias: "$pets.alergias" } }
]).forEach(printjson);

print("\n3. Próximas consultas agendadas");
db.tutores.aggregate([
  { $unwind: "$pets" },
  { $unwind: "$pets.consultas" },
  { $match: { "pets.consultas.status": "AGENDADA" } },
  { $project: {
      _id: 0, tutor: "$nome", pet: "$pets.nome",
      data: "$pets.consultas.dataPrevista", tipo: "$pets.consultas.tipo",
      veterinario: "$pets.consultas.veterinario.nome", clinica: "$pets.consultas.clinica.nome" } },
  { $sort: { data: 1 } }
]).forEach(printjson);

print("\n4. Faturamento por veterinário (consultas realizadas)");
db.tutores.aggregate([
  { $unwind: "$pets" },
  { $unwind: "$pets.consultas" },
  { $match: { "pets.consultas.status": "REALIZADA" } },
  { $group: {
      _id: "$pets.consultas.veterinario.nome",
      consultas: { $sum: 1 },
      faturamento: { $sum: "$pets.consultas.valor" } } },
  { $sort: { faturamento: -1 } }
]).forEach(printjson);

print("\n5. Clínicas com atendimento 24h");
db.clinicas.find(
  { atendimento24h: true },
  { _id: 0, nome: 1, "contato.telefone": 1, "endereco.bairro": 1 }
).forEach(printjson);

print("\n6. Veterinários ordenados pelas estatísticas pré-calculadas no Oracle");
db.veterinarios.find({}, { _id: 0, nome: 1, especialidade: 1, estatisticas: 1 })
  .sort({ "estatisticas.faturamentoTotal": -1 })
  .forEach(printjson);
