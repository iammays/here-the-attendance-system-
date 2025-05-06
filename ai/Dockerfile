# Use slim Python
FROM python:3.12.0-slim

# Set working directory
WORKDIR /app

# Install CMake and system dependencies
RUN apt-get update && apt-get install -y \
    cmake \
    g++ \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python packages
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the app
COPY . .

# Expose the app port
EXPOSE 5000

# Run the app
CMD ["python", "app.py"]
