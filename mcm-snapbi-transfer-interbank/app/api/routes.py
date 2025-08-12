from fastapi import APIRouter, Depends, Header, HTTPException, Request, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from starlette.responses import JSONResponse
import json
import re
import httpx
import random
import logging
from app.core.database import get_db
from app.api.snapbi_client import call_snapbi_transfer_interbank
from app.services.auth import verify_token
from app.models.models import ApiEndpoint, ApiCredential, ApiToken
from app.core.config import DEBUG_MODE, CONSUL_URL, X_PARTNER_ID, CHANNEL_ID

logging.basicConfig(level=logging.DEBUG if DEBUG_MODE else logging.INFO)
logger = logging.getLogger(__name__)

router = APIRouter()

async def get_transaction_signature_service() -> str:
    """Mengambil alamat service dari Consul dengan load balancing."""
    async with httpx.AsyncClient() as client:
        try:
            service_name: str = 'SnapBIGetTransactionSignature'
            consul_endpoint = f"{CONSUL_URL}{service_name}"
            logger.debug(f"Fetching service from Consul: {consul_endpoint}")

            response = await client.get(consul_endpoint)

            if response.status_code == 200 and response.json():
                services = response.json()
                chosen_service = random.choice(services)  # Load balancing (round-robin dapat diterapkan juga)
                service_url = f"http://{chosen_service['ServiceAddress']}:{chosen_service['ServicePort']}/{service_name}"
                logger.debug(f"Selected service instance: {service_url}")
                return service_url
            else:
                logger.error(f"No available instances for service: {service_name}")
                return None
        except Exception as e:
            logger.error(f"Error contacting Consul: {e}")
            return None

@router.post("/SnapBITransferInterbank", summary="Do SNAPBI Transfer Interbank")
async def do_snapbi_transfer_interbank(
    request: Request,
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

    transaction_signature_service = await get_transaction_signature_service()

    if not transaction_signature_service:
        raise HTTPException(status_code=404, detail="SNAPBI Get Transaction Signature Service not found")     
    
    endpoint_transfer_interbank_result = await db.execute(select(ApiEndpoint).where(ApiEndpoint.name == 'SnapBI Transfer Interbank'))
    endpoint_transfer_interbank = endpoint_transfer_interbank_result.scalar()

    if not endpoint_transfer_interbank:
        raise ValueError("SNAPBI SnapBI Transfer Interbank not found in database")    

    try:
        async with httpx.AsyncClient() as client:
            response = await client.request(
                method=request.method,
                url=f"{transaction_signature_service}?HTTPMethod=POST&EndpointUrl={endpoint_transfer_interbank.path}",
                headers=dict(request.headers),
                content=await request.body(),
            )
            response_data = json.dumps(response.json())
            response_data_clean = re.sub(r"[\x00-\x08\x0b-\x1f]", "", response_data)    
            response_data_clean = response_data_clean.replace('\n', '').replace('\r', '').strip()
            
            if isinstance(response_data_clean, str):
                response_data_clean = json.loads(response_data_clean)

            signature_data = response_data_clean.get("signature")
            timestamp_data = response_data_clean.get("timestamp")

            # Call SNAP_BI_balance_inquiry
            snap_bi_response_data = await call_snapbi_transfer_interbank(db, access_token, request_body, signature_data, timestamp_data, X_PARTNER_ID, CHANNEL_ID, endpoint_transfer_interbank)
            
            return snap_bi_response_data                
    except httpx.HTTPError as e:
        logger.error(f"Service request error: {e}")
        return JSONResponse({"error": "Service request failed"}, status_code=500)        
    
