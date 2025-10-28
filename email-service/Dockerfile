# ===================== STAGE 1: Build =====================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Director de lucru
WORKDIR /app

# Copiem fișierele Maven
COPY pom.xml .

# Preluăm dependențele pentru build mai rapid ulterior
RUN mvn dependency:go-offline -B

# Copiem codul sursă
COPY src ./src

# Construim aplicația fără teste
RUN mvn clean package -DskipTests

# ===================== STAGE 2: Runtime =====================
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copiem jar-ul generat
COPY --from=builder /app/target/*.jar app.jar

# Expunem portul aplicației
EXPOSE 8079

# Pornim aplicația
ENTRYPOINT ["java", "-jar", "app.jar"]
