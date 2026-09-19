# ---- build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies separately from source so code edits don't refetch the world.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- run ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Don't run as root.
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app

# Render injects PORT; the app reads it via server.port=${PORT:8081}.
EXPOSE 8081

# Container-aware heap sizing - free tiers are memory-tight (512MB on Render).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
