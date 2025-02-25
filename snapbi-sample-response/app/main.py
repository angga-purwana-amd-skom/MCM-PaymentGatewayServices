"""
Main Project of SNAP BI SAMPLE RESPONSE - MCM - Microservices.
Angga Purwana, AMd., S.Kom.
"""


from fastapi import FastAPI
from app.api.routes import router
from app.models.models import Base
from app.core.database import engine

# Inisialisasi tabel database
async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

app = FastAPI()

# Panggil init_db saat aplikasi dimulai
@app.on_event("startup")
async def startup():
    await init_db()

# Tambahkan route untuk API
app.include_router(router, prefix="")
