# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
RUN chmod +x mvnw
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=builder /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=cloud
EXPOSE 8080

USER spring
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
