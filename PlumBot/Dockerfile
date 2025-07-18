# Buduj projekt za pomocą obrazu Maven + JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Kopiuj pliki projektu do kontenera
COPY pom.xml .
COPY src ./src

# Zbuduj aplikację
RUN mvn clean package -DskipTests

# Użyj obrazu tylko z JRE do uruchamiania
FROM eclipse-temurin:17-jre
WORKDIR /app

# Skopiuj zbudowany plik jar do finalnego obrazu
COPY --from=build /app/target/*.jar app.jar

# Uruchom bota
CMD ["java", "-jar", "app.jar"]
