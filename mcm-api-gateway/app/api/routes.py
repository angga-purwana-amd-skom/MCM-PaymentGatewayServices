from fastapi import APIRouter, Header, HTTPException, Depends



router = APIRouter()


@router.get("/")
async def root():
    return {"message": "API Gateway is running"}

