import aiohttp
import base64
import hashlib
import datetime
import ssl
import re
import json
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.models.models import ApiEndpoint, ApiCredential, ApiEventLog, ApiToken
from app.core.config import DEBUG_MODE

async def generate_signature(client_key: str, private_key: str) -> str:
    """Generate signature for SNAPBI request."""
    timestamp = datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S+00:00")
    data_to_sign = f"{client_key}|{timestamp}".encode()
    signature = base64.b64encode(hashlib.sha256(data_to_sign).digest()).decode()
    return timestamp, signature

async def get_snapbi_token(db: AsyncSession):
    """Fetch token from SNAPBI API and log event."""
    endpoint_result = await db.execute(select(ApiEndpoint).where(ApiEndpoint.name == 'SnapBI Get Token'))
    endpoint = endpoint_result.scalar()

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
            response_data_clean = re.sub(r"[\x00-\x08\x0b-\x1f]", "", response_data)    
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

            return response_data
