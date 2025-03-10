from fastapi import APIRouter, Depends, Header
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from starlette.responses import JSONResponse
import json
import re
from app.core.database import get_db
from app.api.snapbi_client import get_snapbi_token
from app.services.token_service import store_token
from app.services.auth import verify_token
from app.models.models import ApiEndpoint

router = APIRouter()

@router.post("/SnapBIGetTokenByApi", summary="Get SNAPBI Token")
async def get_snapbi_access_token(authorization: str = Header(None), db: AsyncSession = Depends(get_db)):
    """Mengambil token dari SNAPBI dan menyimpannya di DB."""
    
    if not authorization:
        raise HTTPException(status_code=401, detail="Missing Authorization header")

    parts = authorization.split(" ")
    if len(parts) != 2 or parts[0].lower() != "bearer":
        raise HTTPException(status_code=401, detail="Invalid Authorization header format")

    mcmToken = parts[1]
    user = verify_token(mcmToken)

    # Cari endpoint_id untuk SNAPBI
    endpoint_result = await db.execute(select(ApiEndpoint).where(ApiEndpoint.name == 'SnapBI Get Token'))
    endpoint = endpoint_result.scalar()

    if not endpoint:
        raise HTTPException(status_code=500, detail="SNAPBI Get Token Endpoint not found")

    response = await get_snapbi_token(db)
    response = re.sub(r"[\x00-\x08\x0b-\x1f]", "", response)    
    response = response.replace('\n', '').replace('\r', '').strip()

    # Ubah dari string ke dictionary
    if isinstance(response, str):
        response = json.loads(response)

    if response.get("responseCode") == "2007300":
        await store_token(db, endpoint.id, response)
        return response
    else:
        status_code = response.get("status", 500)  # Default ke 500 jika tidak ada status
        return JSONResponse(status_code=status_code, content=response)
