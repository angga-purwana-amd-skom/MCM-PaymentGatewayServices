"""
Main Project of API Gateway - MCM - Microservices.
Powered by FastAPI, starlette, httpx
Presented by Angga Purwana, AMd., S.Kom.
"""

from fastapi import FastAPI, Request, APIRouter, HTTPException
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.responses import JSONResponse
import httpx
import asyncio
import random
import logging
from app.core.config import CONSUL_URL, DEBUG_MODE
from app.api.routes import router

# Konfigurasi logging
logging.basicConfig(level=logging.DEBUG if DEBUG_MODE else logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="FastAPI Payment API Gateway",
    description="A Payment API Gateway with Consul Service Discovery and Load Balancing.",
    version="1.1.0"
)

async def get_service_url(service_name: str) -> str:
    """Mengambil alamat service dari Consul dengan load balancing."""
    async with httpx.AsyncClient() as client:
        try:
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

class ProxyMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        path = request.url.path.lstrip("/")
        
        service_url = await get_service_url(path)
        if not service_url:
            return JSONResponse({"error": "Service not found"}, status_code=404)

        # Ambil query parameters jika ada
        query_params = request.url.query
        if query_params:
            service_url = f"{service_url}?{query_params}"            

        try:
            async with httpx.AsyncClient() as client:
                response = await client.request(
                    method=request.method,
                    url=service_url,
                    headers=dict(request.headers),
                    content=await request.body(),
                )
                return JSONResponse(content=response.json(), status_code=response.status_code)
        except httpx.HTTPError as e:
            logger.error(f"Service request error: {e}")
            return JSONResponse({"error": "Service request failed"}, status_code=500)

app.add_middleware(ProxyMiddleware)
app.include_router(router)

