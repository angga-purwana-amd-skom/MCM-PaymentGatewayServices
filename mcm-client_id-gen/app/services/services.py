from passlib.hash import bcrypt
import uuid
from sqlalchemy.ext.asyncio import AsyncSession
from models.models import Credential

async def generate_credentials(db: AsyncSession):
    mcm_client_id = str(uuid.uuid4())
    mcm_client_secret = bcrypt.hash(mcm_client_id)

    new_credential = Credential(
        mcm_client_id=mcm_client_id,
        mcm_client_secret=bcrypt.hash(mcm_client_secret)
    )

    db.add(new_credential)
    await db.commit()
    return {"mcm_client_id": mcm_client_id, "mcm_client_secret": mcm_client_secret}
