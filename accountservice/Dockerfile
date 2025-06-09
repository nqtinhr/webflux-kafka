# Stage 1
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY ./accountservice ./accountservice
COPY ./commonservice ./commonservice

WORKDIR /app/commonservice
RUN mvn clean install -DskipTests

WORKDIR /app/accountservice
RUN mvn clean package -DskipTests

# Stage 2
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/accountservice/target/*.jar /app/accountservice.jar
EXPOSE 9211
CMD ["java", "-jar", "/app/accountservice.jar"]