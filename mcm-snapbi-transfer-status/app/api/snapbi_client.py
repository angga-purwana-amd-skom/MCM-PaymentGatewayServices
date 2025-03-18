import aiohttp
import base64
import hashlib
import datetime
import ssl
import re
import json
import logging
import time
import random
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.models.models import ApiEndpoint, ApiCredential, ApiEventLog, ApiToken
from app.core.config import DEBUG_MODE

# Konfigurasi logging
logging.basicConfig(level=logging.DEBUG if DEBUG_MODE else logging.INFO)
logger = logging.getLogger(__name__)


def generate_x_external_id():
    """
    Generate a 19-character numeric string unique for the day.
    Format: YYYYMMDD + Unix timestamp in millis (7 digits) + Random 3 digits
    """
    today_str = datetime.datetime.utcnow().strftime('%Y%m%d')  # YYYYMMDD (8 digits)
    timestamp_millis = str(int(time.time() * 1000))[-7:]  # Last 7 digits of Unix millis timestamp
    random_digits = str(random.randint(100, 999))  # 3 random digits
    
    external_id = today_str + timestamp_millis + random_digits
    return external_id

def minify_json(data: dict) -> str:
    """Menghapus spasi dan format dari JSON."""
    return json.dumps(data, separators=(",", ":"), sort_keys=True)

async def call_snapbi_transfer_status(db: AsyncSession, access_token: str, request_body: dict, signature_data: str, timestamp_data: str, x_partner_id: str, channel_id: str,  endpoint_transfer_status: ApiEndpoint) -> str:
    """snapbi_transfer_intrabank sesuai spesifikasi SnapBI."""

    
    # Minify dan hash request body
    minified_body = minify_json(request_body)
    minified_body = json.loads(minified_body)

    if not endpoint_transfer_status:
        raise ValueError("SNAPBI Transfer Status Endpoint not found in database")

    x_external_id = generate_x_external_id()
    
    headers = {
        "Authorization": f"Bearer {access_token}",
        "X-TIMESTAMP": timestamp_data,
        "X-SIGNATURE": signature_data,
        "X-PARTNER-ID": x_partner_id,  
        "X-EXTERNAL-ID": x_external_id,  
        "CHANNEL-ID": channel_id,  
        "Content-Type": "application/json"
    }
    payload = minified_body

    ep_port = "" if endpoint_transfer_status.port == "" else f":{endpoint_transfer_status.port}"
    url = f"https://{endpoint_transfer_status.host}{ep_port}{endpoint_transfer_status.path}"

    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, json=payload, ssl=not DEBUG_MODE) as response:
            response_data = await response.text()
            response_data_clean = re.sub(r"[\x00-\x08\x0b-\x1f]", "", response_data)    
            response_data_clean = response_data_clean.replace('\n', '').replace('\r', '').strip()
            
            if isinstance(response_data_clean, str):
                response_data_clean = json.loads(response_data_clean)

            # Log event
            event_log = ApiEventLog(
                endpoint_id=endpoint_transfer_status.id,
                request_method="POST",
                request_headers=headers,
                request_body=payload,
                response_status_code=response.status,
                response_headers=dict(response.headers),
                response_body=response_data_clean,
            )
            db.add(event_log)
            await db.commit()

            return response_data_clean