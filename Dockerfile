FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV PORT=8023
ENV THYMELEAF_CACHE=true
ENV UPLOAD_DIR=/app/data/uploads

RUN mkdir -p /app/data/uploads

COPY --from=build /app/target/school-transaction-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8023
ENTRYPOINT ["java", "-jar", "app.jar"]
