# Etapa 1: compila y corre las pruebas. Si una prueba falla, la imagen no se construye.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Las dependencias se descargan en una capa aparte para aprovechar la cache de Docker.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B package

# Etapa 2: solo el JRE y el jar, con un usuario sin privilegios.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S banco && adduser -S banco -G banco
COPY --from=build /app/target/banco-api-*.jar app.jar
USER banco

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
