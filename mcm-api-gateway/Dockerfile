# Gunakan base image Python
FROM python:3.9-slim

# Set direktori kerja
WORKDIR /

# Salin file ke image
COPY requirements.txt .
# Install dependensi
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Tentukan environment variables default untuk host dan port
ENV HOST=${HOST}
ENV PORT=${PORT}

ENV CONSUL_URL=${CONSUL_URL}

# env (depcrecated, data get from Consul)
# ENV MCM_CORE_API_AUTH_SERVICE_HOSTNAME_OR_IP=${MCM_CORE_API_AUTH_SERVICE_HOSTNAME_OR_IP}
# ENV MCM_CORE_API_AUTH_SERVICE_PORT=${MCM_CORE_API_AUTH_SERVICE_PORT}
# ENV MCM_CORE_API_SnapBIGetToken_SERVICE_HOSTNAME_OR_IP = os.getenv("MCM_CORE_API_SnapBIGetToken_SERVICE_HOSTNAME_OR_IP", "127.0.0.1")
# ENV MCM_CORE_API_SnapBIGetToken_SERVICE_PORT = os.getenv("MCM_CORE_API_SnapBIGetToken_SERVICE_PORT", "5001")

# Tentukan port yang akan digunakan
EXPOSE $PORT

# Jalankan aplikasi menggunakan Uvicorn dengan konfigurasi dari ENV
CMD ["sh", "-c", "uvicorn app.main:app --host $HOST --port $PORT"]