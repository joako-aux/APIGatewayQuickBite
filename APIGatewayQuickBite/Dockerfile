# --- ETAPA 1: Compilación ---
# Usamos una imagen de Maven que venga con Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el pom.xml para descargar las dependencias en caché
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# --- ETAPA 2: Ejecución ---
# Usamos la imagen ligera de ejecución para Java 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiamos el archivo .jar generado
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto interno de Spring Boot
EXPOSE 8080

# Comando para arrancar el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]