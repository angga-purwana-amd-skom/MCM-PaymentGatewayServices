import asyncio
import datetime
import logging
import aiohttp
import base64
import hashlib
import re
import json
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.core.database import get_db
from app.core.config import DEBUG_MODE
from app.models.models import ApiToken, ApiEndpoint, ApiCredential, ApiEventLog
from app.services.token_service import store_token

# Konfigurasi logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

async def generate_signature(client_key: str, private_key: str) -> str:
    """Generate signature for SNAPBI request."""
    timestamp = datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S+00:00")
    data_to_sign = f"{client_key}|{timestamp}".encode()
    signature = base64.b64encode(hashlib.sha256(data_to_sign).digest()).decode()
    return timestamp, signature

async def get_snapbi_token(db: AsyncSession, endpoint: ApiEndpoint):
    """Fetch token from SNAPBI API and log event."""

    if not endpoint:
        raise ValueError("SNAPBI Endpoint not found in database")

    cred_result = await db.execute(select(ApiCredential).where(ApiCredential.api_name == 'SnapBI'))
    credentials = cred_result.scalar()

    if not credentials:
        raise ValueError("SNAPBI Credentials not found in database")

    timestamp, signature = await generate_signature(credentials.client_key, credentials.private_key)

    headers = {
        "X-CLIENT-KEY": credentials.client_key,
        "X-TIMESTAMP": timestamp,
        "X-SIGNATURE": signature,
        "Content-Type": "application/json",
    }

    payload = {"grantType": "client_credentials"}
    ep_port = "" if endpoint.port == "" else f":{endpoint.port}"
    url = f"https://{endpoint.host}{ep_port}{endpoint.path}"

    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, json=payload, ssl=not DEBUG_MODE) as response:
            response_data = await response.json()
            response_data_str = json.dumps(response_data)  # Convert dict to string
            response_data_clean = re.sub(r"[\x00-\x08\x0b-\x1f]", "", response_data_str)    
            response_data_clean = response_data_clean.replace('\n', '').replace('\r', '').strip()
            
            if isinstance(response_data_clean, str):
                response_data_clean = json.loads(response_data_clean)

            # Log event
            event_log = ApiEventLog(
                endpoint_id=endpoint.id,
                request_method="POST",
                request_headers=headers,
                request_body=payload,
                response_status_code=response.status,
                response_headers=dict(response.headers),
                response_body=response_data_clean,
            )
            db.add(event_log)
            await db.commit()

            return response_data


# Fungsi untuk memperbarui token
async def refresh_token(db: AsyncSession, token: ApiToken, endpoint: ApiEndpoint):
    logging.info(f"Refreshing token {token.id} for endpoint {token.endpoint_id}...")

    response_data = await get_snapbi_token(db, endpoint)
    response_data_str = json.dumps(response_data)  # Convert dict to string
    response_data_clean = re.sub(r"[\x00-\x08\x0b-\x1f]", "", response_data_str)    
    response_data_clean = response_data_clean.replace('\n', '').replace('\r', '').strip()

    # Ubah dari string ke dictionary
    if isinstance(response_data_clean, str):
        response_data_clean = json.loads(response_data_clean)

    if response_data_clean.get("responseCode") != "2007300":
        logging.info(f"Token {token.id} failed to refreshed.. responseCode : {response_data_clean.get('responseCode')}")
        return    

    if response_data_clean.get("responseCode") == "2007300":
        await store_token(db, endpoint.id, response_data_clean)
 

        expires_in = int(response_data_clean.get("expiresIn"))
        new_expires_at = datetime.datetime.utcnow() + datetime.timedelta(seconds=expires_in)
        logging.info(f"Token {token.id} refreshed successfully. New expiry: {new_expires_at}")

# Fungsi untuk mengecek token yang hampir kadaluarsa
async def check_tokens(db: AsyncSession):
    endpoint_gtoken_result = await db.execute(select(ApiEndpoint).where(ApiEndpoint.name == 'SnapBI Get Token'))
    endpoint_gtoken = endpoint_gtoken_result.scalar()

    if not endpoint_gtoken:
        print("Endpoint SnapBI Get Token Not Found")
        return  

    now = datetime.datetime.utcnow()
    threshold_time = now + datetime.timedelta(minutes=5)  # Token dianggap hampir kadaluarsa jika <= 5 menit lagi            
    
    # Ambil token dari database berdasarkan endpoint_id
    token_result = await db.execute(select(ApiToken).filter(ApiToken.endpoint_id == endpoint_gtoken.id, ApiToken.expires_at <= threshold_time))
    api_token = token_result.scalars().first()

    if not api_token:
        print("Token Not Found on Endpoint : SnapBI Get Token") #nothing to do
        return  
    
    #access_token = api_token.access_token
    await refresh_token(db, api_token, endpoint_gtoken)    
    '''
    async with AsyncSessionLocal() as session:
        async with session.begin():
            now = datetime.datetime.utcnow()
            threshold_time = now + datetime.timedelta(minutes=5)  # Token dianggap hampir kadaluarsa jika <= 5 menit lagi
            
            stmt = select(ApiToken).where(ApiToken.expires_at <= threshold_time)
            result = await session.execute(stmt)
            tokens = result.scalars().all()

            for token in tokens:
                logging.warning(f"Token {token.id} hampir atau sudah kadaluarsa! Memperbarui...")
                await refresh_token(token)
    '''            

    #await time.sleep(60)  # Cek setiap 1 menit
