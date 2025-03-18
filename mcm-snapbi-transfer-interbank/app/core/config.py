# app/core/config.py
import os
from dotenv import load_dotenv

load_dotenv()

DEBUG_MODE = os.getenv("DEBUG_MODE", "True").lower() == "true"
CONSUL_URL=os.getenv("CONSUL_URL", "http://127.0.0.1:8500/v1/catalog/service/")

DATABASE_URL = os.getenv("DATABASE_URL")
SECRET_KEY = os.getenv("SECRET_KEY")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
TOKEN_EXPIRATION_MINUTES = int(os.getenv("TOKEN_EXPIRATION_MINUTES", 60))

X_PARTNER_ID = os.getenv("X_PARTNER_ID")
CHANNEL_ID = os.getenv("CHANNEL_ID")

