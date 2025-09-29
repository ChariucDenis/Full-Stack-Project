# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Folosim cache pentru dependențe
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Apoi copiem sursele
COPY src ./src
RUN mvn -q -DskipTests clean package

# ---------- Runtime stage ----------
# (poți folosi eclipse-temurin:17-jdk; dacă vrei alpine, lasă :17-jdk-alpine)
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copiem JAR-ul construit
COPY --from=build /app/target/*.jar app.jar

# IMPORTANT: Render setează un port în $PORT – îl respectăm aici
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT} -jar app.jar"]
