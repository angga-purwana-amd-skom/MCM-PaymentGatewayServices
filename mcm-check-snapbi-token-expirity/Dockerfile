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


CMD ["sh", "-c", "python3.9 -m app.main"]