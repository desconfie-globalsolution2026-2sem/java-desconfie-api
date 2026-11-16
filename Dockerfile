# Etapa 1 (build): compila o projeto com o Maven Wrapper e gera o .jar
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
RUN sh mvnw -q -DskipTests package

# Etapa 2 (execução): imagem menor, só com o Java para rodar o .jar
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Credenciais NÃO ficam na imagem: DB_URL, DB_USER, DB_PASSWORD e IA_URL
# são passadas como variáveis de ambiente (Render ou docker-compose).
ENTRYPOINT ["java", "-jar", "app.jar"]
