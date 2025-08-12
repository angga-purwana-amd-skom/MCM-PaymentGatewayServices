from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
import datetime
from app.models.models import ApiToken

async def store_token(db: AsyncSession, endpoint_id: int, response: dict):
    """Menyimpan atau memperbarui token SNAPBI ke database dengan informasi kedaluwarsa."""
    expires_in = int(response["expiresIn"])
    expires_at = datetime.datetime.utcnow() + datetime.timedelta(seconds=expires_in)

    # Cek apakah token sudah ada untuk endpoint_id
    result = await db.execute(
        select(ApiToken).filter(ApiToken.endpoint_id == endpoint_id)
    )
    existing_token = result.scalars().first()

    if existing_token:
        # Update token yang sudah ada
        existing_token.access_token = response["accessToken"]
        existing_token.token_type = response["tokenType"]
        existing_token.expires_in = expires_in
        existing_token.expires_at = expires_at
    else:
        # Tambahkan token baru jika belum ada
        new_token = ApiToken(
            endpoint_id=endpoint_id,
            access_token=response["accessToken"],
            token_type=response["tokenType"],
            expires_in=expires_in,
            expires_at=expires_at,
        )
        db.add(new_token)

    await db.commit()
