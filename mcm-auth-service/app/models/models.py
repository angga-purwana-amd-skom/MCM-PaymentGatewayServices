# app/models.py
from sqlalchemy import Column, String
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class MCMClient(Base):
    __tablename__ = "mcm_clients"

    mcm_client_id = Column(String, primary_key=True, index=True)
    mcm_client_secret = Column(String, nullable=False)  # Hashed secret
