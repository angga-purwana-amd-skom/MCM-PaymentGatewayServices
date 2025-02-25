"""
SNAPBI GetToken Module of MCM - Microservices.
Powered by FastAPI
Presented by Angga Purwana, AMd., S.Kom.
"""

from fastapi import FastAPI
from app.api.routes import router
from app.core.database import engine
from app.models.models import Base

# Inisialisasi tabel database
async def init_db():
    async with engine.connect() as conn:
        await conn.run_sync(Base.metadata.create_all)

app = FastAPI(title="MCM SNAPBI Get Token Service")

# Panggil init_db saat aplikasi dimulai
@app.on_event("startup")
async def startup():
    await init_db()

app.include_router(router)
