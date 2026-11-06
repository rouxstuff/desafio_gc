# ---------- Etapa 1: compila o projeto (Maven + JDK 21) ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# garante fim de linha Linux no mvnw (evita "bad interpreter" se vier do Windows)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
# baixa as dependências numa camada separada (cache: só refaz se o pom.xml mudar)
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---------- Etapa 2: imagem final (só o Java + o .jar) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
