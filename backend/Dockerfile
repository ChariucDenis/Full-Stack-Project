# Imagine de bază pentru compilare
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copiază tot codul aplicației în container
COPY . .

# Rulează build-ul Maven (fără teste)
RUN mvn clean package -DskipTests

# Imagine de bază pentru runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copiază .jar-ul compilat din containerul anterior
COPY --from=build /app/target/*.jar app.jar

# Expune portul (dacă aplicația rulează pe 8080)
EXPOSE 8080

# Comandă de rulare
ENTRYPOINT ["java", "-jar", "app.jar"]
