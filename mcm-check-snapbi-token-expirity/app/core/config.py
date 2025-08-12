# app/core/config.py
import os
from dotenv import load_dotenv

load_dotenv()

DEBUG_MODE = os.getenv("DEBUG_MODE", "True").lower() == "true"
CONSUL_URL=os.getenv("CONSUL_URL", "http://127.0.0.1:8500/v1/catalog/service/")

DATABASE_URL = os.getenv("DATABASE_URL")