#!/usr/bin/env bash
# =====================================================================
# Deploy do Clyvo PetCare (Java) no Azure App Service
#
# Onde rodar: Azure Cloud Shell (portal.azure.com -> ícone >_ -> Bash)
# Pré-requisito: o código com o frontend já estar no GitHub (push feito).
#
# O banco continua sendo o Oracle da FIAP (schema RM564995).
# O Flyway não cria nada novo: o banco já está atualizado.
# =====================================================================
set -euo pipefail

# ---------- Configurações (ajuste se quiser) ----------
RG="rg-clyvo-java"                       # grupo de recursos novo, só para o Java
PLANO="plan-clyvo-java"                  # plano do App Service
APP="clyvo-petcare-rm564995"             # vira https://clyvo-petcare-rm564995.azurewebsites.net
REGIAO="brazilsouth"                     # se der erro de região/política, troque por eastus
SKU="B1"                                 # B1 = estável para o vídeo; F1 = gratuito, porém lento
REPO="https://github.com/MatheusGianolli/Challenge-java2026.git"

# ---------- 1. Credenciais do Oracle (não ficam salvas em arquivo) ----------
read -rp  "Usuário do Oracle (ex.: RM564995): " DB_USERNAME
read -rsp "Senha do Oracle: " DB_PASSWORD; echo

# ---------- 2. Baixar e compilar o projeto ----------
rm -rf ~/clyvo-build && git clone --depth 1 "$REPO" ~/clyvo-build
cd ~/clyvo-build
chmod +x mvnw
./mvnw -q -DskipTests package
JAR=$(ls target/*.jar | grep -v plain | head -1)
echo "JAR gerado: $JAR"

# ---------- 3. Criar a infraestrutura ----------
az group create --name "$RG" --location "$REGIAO" -o table

az appservice plan create \
  --name "$PLANO" --resource-group "$RG" \
  --is-linux --sku "$SKU" -o table

az webapp create \
  --name "$APP" --resource-group "$RG" --plan "$PLANO" \
  --runtime "JAVA:17-java17" -o table

# ---------- 4. Configurações da aplicação ----------
# SERVER_PORT=80: o App Service encaminha o tráfego para a porta 80
az webapp config appsettings set \
  --name "$APP" --resource-group "$RG" \
  --settings DB_USERNAME="$DB_USERNAME" DB_PASSWORD="$DB_PASSWORD" SERVER_PORT=80 \
  -o none

# Mantém a aplicação "acordada" (não disponível no plano F1)
if [ "$SKU" != "F1" ]; then
  az webapp config set --name "$APP" --resource-group "$RG" --always-on true -o none
fi

# Logs da aplicação visíveis no portal / log tail
az webapp log config --name "$APP" --resource-group "$RG" \
  --application-logging filesystem --level information -o none

# ---------- 5. Publicar o JAR ----------
az webapp deploy \
  --name "$APP" --resource-group "$RG" \
  --src-path "$JAR" --type jar

echo
echo "================================================================"
echo " Publicado! Aguarde ~1 minuto (o Spring + Oracle demoram a subir)"
echo " Interface: https://$APP.azurewebsites.net"
echo " Swagger:   https://$APP.azurewebsites.net/swagger-ui/index.html"
echo " Logs:      az webapp log tail --name $APP --resource-group $RG"
echo "================================================================"
