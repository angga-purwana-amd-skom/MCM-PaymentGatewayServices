from sqlalchemy.ext.asyncio import AsyncSession
import datetime
from app.models.models import ApiToken

async def store_token(db: AsyncSession, endpoint_id: int, response: dict):
    """Menyimpan token SNAPBI ke database dengan informasi kadaluwarsa."""
    expires_in = int(response["expiresIn"])
    expires_at = datetime.datetime.utcnow() + datetime.timedelta(seconds=expires_in)


    token_data = ApiToken(
        endpoint_id=endpoint_id,
        access_token=response["accessToken"],
        token_type=response["tokenType"],
        expires_in=expires_in,
        expires_at=expires_at
    )
    db.add(token_data)
    await db.commit()
    await db.refresh(token_data)