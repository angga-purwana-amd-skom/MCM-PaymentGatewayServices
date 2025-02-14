# app/auth.py
import jwt
import datetime
from fastapi import HTTPException, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from .database import get_db
from .services import validate_credentials
from .config import SECRET_KEY, JWT_ALGORITHM, TOKEN_EXPIRATION_MINUTES

async def generate_token(db: AsyncSession, mcm_client_id: str, mcm_client_secret: str):
    """ Generate JWT token if credentials are valid """
    is_valid = await validate_credentials(db, mcm_client_id, mcm_client_secret)
    if not is_valid:
        raise HTTPException(status_code=401, detail="Invalid credentials")

    expiration = datetime.datetime.utcnow() + datetime.timedelta(minutes=TOKEN_EXPIRATION_MINUTES)
    payload = {
        "sub": mcm_client_id,
        "exp": expiration
    }
    token = jwt.encode(payload, SECRET_KEY, algorithm=JWT_ALGORITHM)
    return token

def verify_token(token: str):
    """ Verify JWT token """
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[JWT_ALGORITHM])
        return payload
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token has expired")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="Invalid token")
