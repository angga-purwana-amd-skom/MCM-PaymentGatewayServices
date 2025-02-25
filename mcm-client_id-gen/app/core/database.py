from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker
from app.core.config import DATABASE_URL

# Buat engine untuk PostgreSQL (gunakan asyncpg)
engine = create_async_engine(DATABASE_URL, future=True, echo=True)

# Buat session factory
async_session = sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)

# Fungsi untuk mendapatkan session database
async def get_db():
    async with async_session() as session:
        yield session
