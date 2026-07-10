# ---- Build ----
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app
COPY pom.xml .
# Pré-chauffage du cache de dépendances (non bloquant : package complètera au besoin)
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline || true
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests

# ---- Runtime ----
FROM eclipse-temurin:24-jre
RUN useradd -r -m app \
    && mkdir -p /data/uploads /data/pdfs && chown -R app:app /data
USER app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
