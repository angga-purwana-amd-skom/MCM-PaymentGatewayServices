from fastapi import APIRouter, Depends, Header, HTTPException, Request, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from starlette.responses import JSONResponse
import json
import re
from app.core.database import get_db
from app.api.snapbi_client import generate_signature
from app.services.auth import verify_token
from app.models.models import ApiEndpoint, ApiCredential, ApiToken

router = APIRouter()

@router.post("/SnapBIGetTransactionSignature", summary="Get SNAPBI Signature")
async def get_snapbi_signature(
    request: Request,
    http_method: str = Query(..., alias="HTTPMethod"),
    endpoint_url: str = Query(..., alias="EndpointUrl"),
    authorization: str = Header(None),
    db: AsyncSession = Depends(get_db)
):
    if not authorization:
        raise HTTPException(status_code=401, detail="Missing Authorization header")

    parts = authorization.split(" ")
    if len(parts) != 2 or parts[0].lower() != "bearer":
        raise HTTPException(status_code=401, detail="Invalid Authorization header format")

    mcmToken = parts[1]
    user = verify_token(mcmToken)

    if not http_method or not endpoint_url:
        raise HTTPException(status_code=400, detail="HTTPMethod and EndpointUrl parameter are required")
    
    # Ambil API Credential berdasarkan endpoint_id
    credential_result = await db.execute(select(ApiCredential).filter(ApiCredential.api_name == 'SnapBI'))
    credential = credential_result.scalars().first()

    if not credential:
        raise HTTPException(status_code=401, detail="Invalid API Credential")

    private_key = credential.private_key  # Ambil private key dari database

    endpoint_gtoken_result = await db.execute(select(ApiEndpoint).where(ApiEndpoint.name == 'SnapBI Get Token'))
    endpoint_gtoken = endpoint_gtoken_result.scalar()

    if not endpoint_gtoken:
        raise HTTPException(status_code=500, detail="SNAPBI Get Token Endpoint not found")    
    
    # Ambil token dari database berdasarkan endpoint_id
    token_result = await db.execute(select(ApiToken).filter(ApiToken.endpoint_id == endpoint_gtoken.id))
    api_token = token_result.scalars().first()

    if not api_token:
        raise HTTPException(status_code=401, detail="Invalid API Token")
    
    access_token = api_token.access_token
    
    # Ambil request body (jika ada)
    try:
        request_body = await request.json()
    except:
        request_body = {}
    
    # Generate signature menggunakan private_key dari database
    signature, timestamp = generate_signature(http_method, endpoint_url, access_token, request_body, private_key)
    
    return {"signature": signature, "timestamp": timestamp}