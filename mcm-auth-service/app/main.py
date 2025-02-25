# app/main.py
"""
Auth Module of API Gateway - MCM - Microservices.
Powered by FastAPI
Presented by Angga Purwana, AMd., S.Kom.
"""

from fastapi import FastAPI
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.models import Base
from app.core.database import engine
from app.api.routes import router

# Inisialisasi tabel database
async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

app = FastAPI(
    title="FastAPI Payment Gateway Auth Module",
    description="An Payment Gateway API with Auth Module Service.",
    version="1.0.0"
)

# Panggil init_db saat aplikasi dimulai
@app.on_event("startup")
async def startup():
    await init_db()

# Tambahkan route credential
app.include_router(router)
