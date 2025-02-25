from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.services.services import generate_credentials

router = APIRouter()

@router.get("/generate")
async def create_credentials(db: AsyncSession = Depends(get_db)):
    return await generate_credentials(db)
