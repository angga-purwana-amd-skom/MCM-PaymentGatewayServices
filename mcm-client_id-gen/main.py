"""
Main Project of Client Id Gen - MCM - Microservices.
Angga Purwana, AMd., S.Kom.
"""

from fastapi import FastAPI
from routes import credential
import models
from database import engine

# Inisialisasi tabel database
async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(models.Base.metadata.create_all)

app = FastAPI()

# Panggil init_db saat aplikasi dimulai
@app.on_event("startup")
async def startup():
    await init_db()

# Tambahkan route credential
app.include_router(credential.router)
