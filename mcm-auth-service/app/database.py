# app/database.py
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from .config import DATABASE_URL


engine = create_async_engine(DATABASE_URL, future=True, echo=True)

async_session = sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)

# Fungsi untuk mendapatkan session database
async def get_db():
    async with async_session() as session:
        yield session