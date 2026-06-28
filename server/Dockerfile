# Build stage — compiles the Ktor server fat JAR
# NOTE: requires internet access (e.g. GitHub Actions).
# For local builds behind a corporate proxy, pass Gradle proxy settings via build-arg:
#   docker build -f server/Dockerfile -t mykitchen-server \
#     --build-arg GRADLE_PROXY_OPTS="-Dhttp.proxyHost=<host> -Dhttp.proxyPort=<port> -Dhttps.proxyHost=<host> -Dhttps.proxyPort=<port>" .
FROM eclipse-temurin:25.0.3_9-jdk-jammy AS builder
# Optional proxy settings for Gradle (Java system properties).
# Leave empty for GitHub Actions (no proxy needed there).
ARG GRADLE_PROXY_OPTS=""
ENV GRADLE_OPTS=$GRADLE_PROXY_OPTS
WORKDIR /app
COPY . .
RUN ./gradlew :server:buildFatJar --no-daemon --no-configuration-cache

# Runtime stage — slim JRE image with only the JAR
FROM eclipse-temurin:25.0.3_9-jre-jammy
WORKDIR /app
COPY --from=builder /app/server/build/libs/server-all.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]
