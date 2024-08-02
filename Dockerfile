# Build stage
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package assembly:single

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/mirrorftp-1.0-SNAPSHOT-jar-with-dependencies.jar ./mirrorftp.jar

ENTRYPOINT ["java", "-jar", "mirrorftp.jar"]

CMD ["--add-storage-nodes=/storage"]
