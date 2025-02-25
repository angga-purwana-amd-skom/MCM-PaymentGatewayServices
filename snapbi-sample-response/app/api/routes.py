from fastapi import APIRouter, Request, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from starlette.responses import JSONResponse
from app.core.database import get_db
from app.models.models import SampleResponse
from pydantic import BaseModel
import sys

router = APIRouter()

# Schema untuk request penyimpanan
class ResponseInput(BaseModel):
    path_url: str
    sample_response: str
    http_code: str

@router.post("/store_response")
async def store_response(data: ResponseInput, db: AsyncSession = Depends(get_db)):
    """Simpan response berdasarkan path_url."""
    # Cek apakah path_url sudah ada
    result = await db.execute(select(SampleResponse).where(SampleResponse.path_url == data.path_url))
    existing = result.scalars().first()

    if existing:
        existing.sample_response = data.sample_response
    else:
        new_entry = SampleResponse(path_url=data.path_url, sample_response=data.sample_response)
        db.add(new_entry)
    
    await db.commit()
    return {"message": "Response saved successfully"}




@router.api_route("/{path_url:path}", methods=["GET", "POST"])
async def get_response(request: Request, db: AsyncSession = Depends(get_db)):
    """Ambil response berdasarkan path_url."""
    path_url = request.url.path  # Ambil path URL yang diakses oleh client   
    print("path_url :", path_url)
    sys.stdout.flush()

    result = await db.execute(select(SampleResponse).where(SampleResponse.path_url == path_url))
    response = result.scalars().first()


    if not response:
        raise HTTPException(status_code=404, detail="Response not found")
    if response.http_code != '200':
        status_code = int(response.http_code)  # Default ke 500 jika tidak ada status
        return JSONResponse(status_code=status_code, content=response.sample_response)
    return response.sample_response
   
