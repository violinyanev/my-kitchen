# Use the official OpenJDK image as the base image
FROM openjdk:17-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the distribution to the container
COPY build/distributions/backend.tar ./

# Extract the distribution
RUN tar -xf backend.tar

# Set environment variables for container deployment
ENV FLASK_HOST=0.0.0.0
ENV FLASK_PORT=5000

# Expose port 5000 for the Ktor app
EXPOSE 5000

# Start the Ktor app
CMD ["./backend/bin/backend"]