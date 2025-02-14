# app/main.py
"""
Auth Module of API Gateway - MCM - Microservices.
Powered by FastAPI
Presented by Angga Purwana, AMd., S.Kom.
"""

from fastapi import FastAPI, Header, HTTPException, Depends
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from .auth import generate_token, verify_token
from .models import Base
from .database import engine, get_db

# Inisialisasi tabel database
async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

app = FastAPI(
    title="FastAPI Payment Gateway Auth Module",
    description="An Payment Gateway API with Auth Module Service.",
    version="1.0.0"
)

# Panggil init_db saat aplikasi dimulai
@app.on_event("startup")
async def startup():
    await init_db()

class AuthRequest(BaseModel):
    mcm_client_id: str
    mcm_client_secret: str



@app.post("/token", summary="Get Token", description="Get Token")
async def get_token(auth: AuthRequest, db: AsyncSession = Depends(get_db)):
    """ Endpoint to generate access token """
    token = await generate_token(db, auth.mcm_client_id, auth.mcm_client_secret)
    return {"access_token": token, "token_type": "bearer"}

@app.get("/secure-data")
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
