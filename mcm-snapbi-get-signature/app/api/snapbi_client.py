import aiohttp
import base64
import hashlib
import datetime
import ssl
import re
import json
import hmac
import base64
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.models.models import ApiEndpoint, ApiCredential, ApiEventLog, ApiToken
from app.core.config import DEBUG_MODE

def minify_json(data: dict) -> str:
    """Menghapus spasi dan format dari JSON sebelum hashing."""
    return json.dumps(data, separators=(",", ":"), sort_keys=True)

def generate_signature(http_method: str, endpoint_url: str, access_token: str, request_body: dict, private_key: str) -> str:
    """Membuat signature sesuai spesifikasi SnapBI."""
    timestamp = datetime.datetime.utcnow().isoformat() + "Z"
    
    # Minify dan hash request body
    minified_body = minify_json(request_body)
    sha256_hash = hashlib.sha256(minified_body.encode('utf-8')).hexdigest().lower()
    
    # Format data yang akan di-hash
    data = f"{http_method}:{endpoint_url}:{access_token}:{sha256_hash}:{timestamp}"
    
    # Buat HMAC SHA-512 signature dengan private key dari database
    hmac_signature = hmac.new(private_key.encode('utf-8'), data.encode('utf-8'), hashlib.sha512).digest()
    
    # Encode ke base64
    signature = base64.b64encode(hmac_signature).decode('utf-8')
    
    return signature, timestamp