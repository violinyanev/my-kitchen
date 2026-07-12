# Build stage — compiles the Kotlin/WasmJs web distribution
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :webApp:wasmJsBrowserDistribution --no-daemon --no-configuration-cache

# Runtime stage — Nginx serving the static files
FROM nginx:alpine
COPY --from=builder /app/webApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 80
