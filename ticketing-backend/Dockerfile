# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring

COPY app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=cloud
EXPOSE 8080

USER spring
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
