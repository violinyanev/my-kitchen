# Use the official Python image as the base image
FROM python:3.14-slim

# Set the working directory inside the container
WORKDIR /app

COPY --from=ghcr.io/astral-sh/uv:latest /uv /usr/local/bin/uv

# Copy the project definition and install dependencies
COPY pyproject.toml .
RUN uv pip install --system .

# Copy the Flask app code to the container
COPY app.py .

COPY recipes/ ./recipes/

COPY auth/ ./auth/

COPY users/ ./users/

# Set environment variables for container deployment
ENV FLASK_HOST=0.0.0.0
ENV FLASK_PORT=5000

# Expose port 5000 for the Flask app
EXPOSE 5000

# Start the Flask app
CMD ["python", "app.py"]
