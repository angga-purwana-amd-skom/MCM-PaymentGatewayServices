# Gunakan base image Python
FROM python:3.9-slim

# Set direktori kerja
WORKDIR /app

# Salin file requirements.txt ke dalam container
COPY requirements.txt .

# Install dependensi
RUN pip install --no-cache-dir -r requirements.txt

# Salin semua file ke dalam container
COPY . .

# Gunakan environment variables
ENV DATABASE_URL=${DATABASE_URL}
ENV SECRET_KEY=${SECRET_KEY}
ENV JWT_ALGORITHM=${JWT_ALGORITHM}
ENV TOKEN_EXPIRATION_MINUTES=${TOKEN_EXPIRATION_MINUTES}


# Tentukan environment variables untuk host dan port
ENV HOST=${HOST}
ENV PORT=${PORT}

# Tentukan port yang akan digunakan
EXPOSE $PORT

# Jalankan aplikasi menggunakan Uvicorn dengan konfigurasi dari ENV
CMD ["sh", "-c", "uvicorn app.main:app --host $HOST --port $PORT"]