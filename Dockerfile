# Etapa de dependencias (cache de Maven)
FROM eclipse-temurin:24-jdk AS dependencies
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Etapa de compilacion
FROM eclipse-temurin:24-jdk AS compile
WORKDIR /app
COPY --from=dependencies /root/.m2 /root/.m2
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -B

# Etapa de produccion (solo JRE)
FROM eclipse-temurin:24-jre AS prod
WORKDIR /app
# curl se usa en el healthcheck del docker-compose
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=compile /app/target/bdget-0.0.1-SNAPSHOT.jar /app/bdget.jar
ENV TNS_ADMIN=/app/wallet
RUN useradd -r -u 1001 appuser
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/bdget.jar"]