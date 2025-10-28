# ===================== STAGE 1: Build =====================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Setăm directorul de lucru
WORKDIR /app

# Copiem fișierele de configurare Maven
COPY pom.xml .

# Descărcăm dependențele (cache pentru build mai rapid)
RUN mvn dependency:go-offline -B

# Copiem sursele și construim aplicația
COPY src ./src
RUN mvn clean package -DskipTests

# ===================== STAGE 2: Runtime =====================
FROM eclipse-temurin:17-jdk-jammy

# Setăm directorul de lucru
WORKDIR /app

# Copiem jar-ul generat din etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Expunem portul intern
EXPOSE 8084

# Pornim aplicația Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
