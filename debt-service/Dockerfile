FROM maven:3.9.16-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl debt-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/debt-service/target/debt-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
