# Use the official Python image as the base image
FROM python:3.8-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the requirements file to the container
COPY requirements.txt .

# Install Flask and other dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the Flask app code to the container
COPY app.py .

COPY recipes/ ./recipes/

COPY auth/ ./auth/

COPY users/ ./users/

# Expose port 5000 for the Flask app
EXPOSE 5000

# Start the Flask app
CMD ["python", "app.py"]
