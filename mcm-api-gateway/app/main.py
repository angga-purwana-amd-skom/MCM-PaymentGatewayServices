"""
Main Project of API Gateway - MCM - Microservices.
Powered by FastAPI, starlette, httpx
Presented by Angga Purwana, AMd., S.Kom.
"""

from fastapi import FastAPI, Request
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.responses import JSONResponse
import httpx
import asyncio
from config import CONSUL_URL , DEBUG_MODE

app = FastAPI(
    title="FastAPI Payment API Gateway",
    description="A Payment API Gateway with Consul Service Discovery.",
    version="1.0.0"
)

async def get_service_url(service_name: str) -> str:
    """Mengambil alamat service dari Consul."""
    async with httpx.AsyncClient() as client:
        if DEBUG_MODE:
            print(f"DEBUG: consul url svc=> {CONSUL_URL}{service_name}")        
        response = await client.get(f"{CONSUL_URL}{service_name}")
        
        if response.status_code == 200 and response.json():
            service = response.json()[0]  # Ambil instance pertama jika ada banyak
            return f"http://{service['ServiceAddress']}:{service['ServicePort']}/{service_name}"

    return None

class ProxyMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        path = request.url.path.lstrip("/")
        
        # Ambil service dari Consul
        service_url = await get_service_url(path)
        if DEBUG_MODE:
            print(f"DEBUG: service_url= {service_url}")       
        
        if not service_url:
            return JSONResponse({"error": "Service not found"}, status_code=404)

        async with httpx.AsyncClient() as client:
            response = await client.request(
                method=request.method,
                url=service_url,
                headers=request.headers.raw,
                content=await request.body(),
            )

        return JSONResponse(content=response.json(), status_code=response.status_code)

# Tambahkan middleware proxy
app.add_middleware(ProxyMiddleware)
