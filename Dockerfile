# Etapa 1: Compilar la aplicación Spring Boot con Java 21
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa 2: Imagen ligera de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Optimización de memoria para planes gratuitos (512MB RAM) y puerto dinámico
ENV JAVA_TOOL_OPTIONS="-Xmx350m -Xss512k"
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
