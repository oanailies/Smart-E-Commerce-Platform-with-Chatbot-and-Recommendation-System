# ===================== STAGE 1: Build =====================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiem fișierele pom.xml și sursele
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Buildăm aplicația (jar-ul)
RUN mvn clean package -DskipTests

# ===================== STAGE 2: Run =====================
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copiem doar jar-ul final din primul container
COPY --from=build /app/target/*.jar app.jar

# Expunem portul
EXPOSE 8083

# Comanda de start
ENTRYPOINT ["java", "-jar", "app.jar"]
