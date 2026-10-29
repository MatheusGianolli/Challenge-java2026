# =====================================================================
# 02_importacao.ps1 — Importa os JSON exportados do Oracle para o MongoDB
#
# Os arquivos vêm do endpoint GET /api/relatorios/exportacao/{colecao},
# que chama a função pkg_clyvo.exportar_colecao_json no Oracle.
#
# Uso (na pasta database\mongodb):
#   .\02_importacao.ps1                                   -> MongoDB local
#   .\02_importacao.ps1 -Uri "mongodb+srv://usuario:senha@cluster..." -> Atlas
#
# Requer o MongoDB Database Tools (mongoimport) e o mongosh no PATH.
# =====================================================================
param(
    [string]$Uri = "mongodb://localhost:27017"
)

$ErrorActionPreference = "Stop"
$pastaJson = Join-Path $PSScriptRoot "..\json"

# 1. Cria (ou recria) as coleções com validação e índices
mongosh $Uri --quiet --file (Join-Path $PSScriptRoot "01_criacao.js")

# 2. Importa cada arquivo para a coleção de mesmo nome
foreach ($colecao in @("clinicas", "veterinarios", "tutores")) {
    $arquivo = Join-Path $pastaJson "$colecao.json"
    Write-Host "Importando $arquivo -> clyvo.$colecao"
    mongoimport --uri $Uri --db clyvo --collection $colecao --file $arquivo --jsonArray
}

# 3. Conferência
mongosh $Uri --quiet --eval @"
db = db.getSiblingDB('clyvo');
print('tutores:      ' + db.tutores.countDocuments());
print('veterinarios: ' + db.veterinarios.countDocuments());
print('clinicas:     ' + db.clinicas.countDocuments());
"@
