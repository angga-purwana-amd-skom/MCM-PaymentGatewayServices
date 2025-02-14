# Gunakan base image Python
FROM python:3.9-slim

# Set working directory
WORKDIR /app

# Salin dan install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Salin semua file ke dalam container
COPY . .

# Gunakan environment variables
ENV DATABASE_URL=${DATABASE_URL}
ENV SECRET_KEY=${SECRET_KEY}

# Tentukan environment variables untuk host dan port
ENV HOST=${HOST}
ENV PORT=${PORT}

# Tentukan port yang akan digunakan
EXPOSE $PORT


# Jalankan aplikasi dengan Uvicorn
CMD ["sh", "-c", "uvicorn main:app --host $HOST --port $PORT"]
