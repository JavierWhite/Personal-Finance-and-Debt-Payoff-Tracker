FROM maven:3.9.16-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl config-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/config-service/target/config-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
