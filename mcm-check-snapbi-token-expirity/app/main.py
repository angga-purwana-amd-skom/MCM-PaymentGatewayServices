"""
MCM - Microservices ~ Check SNAPBI TOKEN EXPIRITY.
Presented by Angga Purwana, AMd., S.Kom.
"""



from sqlalchemy.ext.asyncio import AsyncEngine
import asyncio
import logging
import signal
import time
from app.services.check_expiring_token import check_tokens
from app.core.database import engine, get_db
from app.models.models import Base
from app.core.config import DATABASE_URL

# Inisialisasi tabel database
async def init_db():
    async with engine.connect() as conn:
        await conn.run_sync(Base.metadata.create_all)



# Konfigurasi Logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")


engine: AsyncEngine = engine  # Ensure consistent engine usage        

async def shutdown():
    logging.info("Shutting down gracefully...")
    await engine.dispose()


loop = asyncio.get_event_loop()

def graceful_shutdown():
    asyncio.create_task(shutdown())  # Schedule shutdown as an async task
    loop.stop()

# Register signal handlers for graceful shutdown
for sig in (signal.SIGTERM, signal.SIGINT):
    loop.add_signal_handler(sig, graceful_shutdown)

# Function to run token check
async def main():
    logging.info("Memulai pengecekan token...")
    async for db in get_db():
        asyncio.create_task(check_tokens(db))  # Run as background task
        break  


if __name__ == "__main__":
    #asyncio.set_event_loop_policy(asyncio.DefaultEventLoopPolicy())
    #asyncio.run(init_db())
    #asyncio.run(main())
    while True:
        loop.run_until_complete(init_db())
        loop.run_until_complete(main())
        time.sleep(60)
    #loop.run_forever()  # Keep the loop running