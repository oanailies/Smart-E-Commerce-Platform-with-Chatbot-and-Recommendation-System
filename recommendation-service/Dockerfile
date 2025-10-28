# ===================== STAGE 1: Build =====================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Setăm directorul de lucru
WORKDIR /app

# Copiem fișierele Maven
COPY pom.xml .

# Preluăm dependențele pentru a accelera buildurile ulterioare
RUN mvn dependency:go-offline -B

# Copiem sursele aplicației
COPY src ./src

# Construim aplicația fără teste (mai rapid pentru Docker)
RUN mvn clean package -DskipTests

# ===================== STAGE 2: Runtime =====================
FROM eclipse-temurin:17-jdk-jammy

# Directorul de lucru în container
WORKDIR /app

# Copiem JAR-ul construit
COPY --from=builder /app/target/*.jar app.jar

# Expunem portul aplicației
EXPOSE 8085

# Pornim aplicația Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
