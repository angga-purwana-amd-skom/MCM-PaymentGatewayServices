# app/core/config.py
import os
from dotenv import load_dotenv

load_dotenv()

DEBUG_MODE = os.getenv("DEBUG_MODE", "True").lower() == "true"

DATABASE_URL = os.getenv("DATABASE_URL")
SECRET_KEY = os.getenv("SECRET_KEY")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
TOKEN_EXPIRATION_MINUTES = int(os.getenv("TOKEN_EXPIRATION_MINUTES", 60))
