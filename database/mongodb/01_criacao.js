// =====================================================================
// 01_criacao.js — Estrutura do banco MongoDB "clyvo"
// Executar com:  mongosh "<uri>" --file 01_criacao.js
//   (local: mongosh "mongodb://localhost:27017" --file 01_criacao.js)
//
// Modelo de documentos (derivado do Oracle pelo pkg_clyvo):
//   tutores      -> tutor com seus pets EMBUTIDOS e, em cada pet,
//                   o histórico de consultas EMBUTIDO. O que é lido
//                   junto (tutor + pets + histórico) fica num só documento.
//   veterinarios -> coleção própria (é compartilhado por muitos tutores);
//                   nas consultas fica só um resumo (id, nome, crmv).
//   clinicas     -> coleção própria pelo mesmo motivo.
// =====================================================================

db = db.getSiblingDB("clyvo");

// Recria do zero para o script poder ser executado várias vezes
["tutores", "veterinarios", "clinicas"].forEach(nome => {
  if (db.getCollectionNames().includes(nome)) {
    db.getCollection(nome).drop();
    print("Coleção removida: " + nome);
  }
});

// Tipos reutilizados nas validações
const texto      = { bsonType: ["string", "null"] };
const data       = { bsonType: ["date", "null"] };
const numero     = { bsonType: ["number", "null"] };
const booleano   = { bsonType: ["bool", "null"] };
const status     = { enum: ["ATIVO", "INATIVO", null] };

const endereco = {
  bsonType: ["object", "null"],
  properties: {
    logradouro: texto, numero: texto, bairro: texto,
    cidade: texto, estado: texto, cep: texto
  }
};

// ---------------------------------------------------------------------
// tutores (com pets e consultas embutidos)
// ---------------------------------------------------------------------
db.createCollection("tutores", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "nome", "documento", "contato", "pets"],
      properties: {
        _id:             { bsonType: "number", description: "ID do tutor no Oracle (TB_TUTORES.ID)" },
        nome:            { bsonType: "string" },
        documento: {
          bsonType: "object",
          required: ["numero"],
          properties: { tipo: { enum: ["CPF", "CNPJ", null] }, numero: { bsonType: "string" } }
        },
        dataNascimento:  data,
        tipoResponsavel: texto,
        contato: {
          bsonType: "object",
          properties: {
            email: texto, telefone: texto, preferencia: texto,
            recebeNotificacao: booleano, autorizaMonitoramento: booleano
          }
        },
        endereco:        endereco,
        status:          status,
        dataCadastro:    data,
        pets: {
          bsonType: "array",
          items: {
            bsonType: "object",
            required: ["petId", "nome", "especie", "consultas"],
            properties: {
              petId: { bsonType: "number" }, nome: { bsonType: "string" },
              especie: { bsonType: "string" }, raca: texto, dataNascimento: data,
              sexo: { enum: ["M", "F", null] }, porte: texto,
              peso: numero, altura: numero, statusSaude: texto, alergias: texto,
              castrado: booleano, corPelagem: texto, microchip: texto,
              consultas: {
                bsonType: "array",
                items: {
                  bsonType: "object",
                  required: ["consultaId", "dataPrevista", "status"],
                  properties: {
                    consultaId: { bsonType: "number" },
                    dataPrevista: { bsonType: "date" },
                    retornoPrevisto: data,
                    tipo: texto,
                    status: { enum: ["AGENDADA", "REALIZADA", "CANCELADA"] },
                    valor: numero, sintomas: texto, diagnostico: texto,
                    veterinario: { bsonType: "object" },
                    clinica: { bsonType: "object" }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
});
print("Coleção criada: tutores");

// ---------------------------------------------------------------------
// veterinarios
// ---------------------------------------------------------------------
db.createCollection("veterinarios", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "nome", "crmv"],
      properties: {
        _id: { bsonType: "number" }, nome: { bsonType: "string" }, crmv: { bsonType: "string" },
        especialidade: texto, tipoAtuacao: texto,
        contato: { bsonType: ["object", "null"], properties: { email: texto, telefone: texto } },
        status: status, dataCadastro: data,
        estatisticas: {
          bsonType: ["object", "null"],
          properties: { totalConsultas: numero, consultasRealizadas: numero, faturamentoTotal: numero }
        }
      }
    }
  }
});
print("Coleção criada: veterinarios");

// ---------------------------------------------------------------------
// clinicas
// ---------------------------------------------------------------------
db.createCollection("clinicas", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "nome", "cnpj"],
      properties: {
        _id: { bsonType: "number" }, nome: { bsonType: "string" }, cnpj: { bsonType: "string" },
        tipoEstabelecimento: texto, especialidadePrincipal: texto,
        atendimento24h: booleano, atendeEmergencia: booleano,
        contato: { bsonType: ["object", "null"], properties: { email: texto, telefone: texto } },
        endereco: endereco, status: status, dataCadastro: data
      }
    }
  }
});
print("Coleção criada: clinicas");

// ---------------------------------------------------------------------
// Índices (pensados nas consultas da aplicação)
// ---------------------------------------------------------------------
db.tutores.createIndex({ "documento.numero": 1 }, { unique: true, name: "uk_tutor_documento" });
db.tutores.createIndex({ "contato.email": 1 }, { name: "idx_tutor_email" });
db.tutores.createIndex({ "pets.microchip": 1 }, { name: "idx_pet_microchip" });
db.tutores.createIndex({ "pets.consultas.dataPrevista": 1 }, { name: "idx_consulta_data" });
db.tutores.createIndex({ "pets.consultas.veterinario.id": 1 }, { name: "idx_consulta_veterinario" });

db.veterinarios.createIndex({ crmv: 1 }, { unique: true, name: "uk_veterinario_crmv" });
db.veterinarios.createIndex({ especialidade: 1 }, { name: "idx_veterinario_especialidade" });

db.clinicas.createIndex({ cnpj: 1 }, { unique: true, name: "uk_clinica_cnpj" });
db.clinicas.createIndex({ "endereco.cidade": 1, atendimento24h: 1 }, { name: "idx_clinica_cidade_24h" });

print("Índices criados. Estrutura pronta: " + db.getCollectionNames().join(", "));
