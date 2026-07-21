FROM maven:3.9.16-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl user-account-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/user-account-service/target/user-account-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
