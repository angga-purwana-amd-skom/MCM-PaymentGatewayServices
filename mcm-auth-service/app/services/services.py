# app/services.py
from passlib.hash import bcrypt
import uuid
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.models.models import MCMClient



async def validate_credentials(db: AsyncSession, mcm_client_id: str, mcm_client_secret: str):
    """ Validate client credentials """
    client = await db.execute(
        select(MCMClient).where(MCMClient.mcm_client_id == mcm_client_id)
    )
    client = client.scalars().first()

    if client and bcrypt.verify(mcm_client_secret, client.mcm_client_secret):
        return True
    return False