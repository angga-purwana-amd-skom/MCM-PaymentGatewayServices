from fastapi import APIRouter, Header, HTTPException, Depends
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.services.auth import generate_token, verify_token


router = APIRouter()


class AuthRequest(BaseModel):
    mcm_client_id: str
    mcm_client_secret: str



@router.post("/token", summary="Get Token", description="Get Token")
async def get_token(auth: AuthRequest, db: AsyncSession = Depends(get_db)):
    """ Endpoint to generate access token """
    token = await generate_token(db, auth.mcm_client_id, auth.mcm_client_secret)
    return {"access_token": token, "token_type": "bearer"}

@router.get("/secure-data")
async def secure_endpoint(authorization: str = Header(None, convert_underscores=False)):
    """ Protected API endpoint requiring Bearer token """
    if not authorization:
        raise HTTPException(status_code=401, detail="Missing Authorization header")

    parts = authorization.split(" ")
    if len(parts) != 2 or parts[0].lower() != "bearer":
        raise HTTPException(status_code=401, detail="Invalid Authorization header format")

    token = parts[1]
    user = verify_token(token)

    return {"message": "Access granted!", "user": user}