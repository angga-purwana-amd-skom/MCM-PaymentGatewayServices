from sqlalchemy import Column, String
from sqlalchemy.ext.declarative import declarative_base
import uuid

Base = declarative_base()

class Credential(Base):
    __tablename__ = "mcm_clients"

    mcm_client_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    mcm_client_secret = Column(String, nullable=False)
