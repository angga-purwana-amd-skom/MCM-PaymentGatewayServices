FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt . 
RUN pip install --no-cache-dir -r requirements.txt

COPY . .
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

ENV DATABASE_URL=${DATABASE_URL}

ENV HOST=${HOST}
ENV PORT=${PORT}

EXPOSE $PORT

# Jalankan aplikasi dengan Uvicorn
CMD ["sh", "-c", "uvicorn app.main:app --host $HOST --port $PORT --ssl-keyfile=key.pem --ssl-certfile=cert.pem"]
