# =====================================================================
# Clyvo PetCare - imagem Docker da API Java (Spring Boot)
# Build em duas etapas: compila com Maven e roda só com o JRE.
# =====================================================================

# ---------- Etapa 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Baixa as dependências primeiro (aproveita o cache do Docker)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Compila o projeto
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Etapa 2: execução ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Roda sem privilégios de administrador
RUN useradd --system --create-home clyvo
USER clyvo

COPY --from=build /app/target/*.jar app.jar

# Memória ajustada para instâncias pequenas (512 MB, como o plano grátis do Render):
# heap + metaspace + code cache ficam abaixo do limite do container
ENV JAVA_OPTS="-XX:MaxRAMPercentage=50 -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=48m -XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xss512k"

# O Render (e outros provedores) informa a porta pela variável PORT
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
