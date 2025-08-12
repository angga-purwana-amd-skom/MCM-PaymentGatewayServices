from sqlalchemy import Column, String, Text, Integer, Boolean, DateTime, ForeignKey, JSON
from sqlalchemy.sql import func
from sqlalchemy.orm import declarative_base, relationship
import datetime

Base = declarative_base()

class ApiCredential(Base):
    __tablename__ = "api_credentials"

    id = Column(Integer, primary_key=True, autoincrement=True)
    api_name = Column(String(255), nullable=False)
    client_key = Column(String(255), nullable=False)
    private_key = Column(Text, nullable=False)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())

class ApiEndpoint(Base):
    __tablename__ = "api_endpoints"

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(255), nullable=False)
    host = Column(String(255), nullable=False)
    port = Column(Integer, nullable=True)
    path = Column(String(255), nullable=False)
    method = Column(String(10), nullable=False)
    headers = Column(JSON, nullable=True)
    request_body_template = Column(JSON, nullable=True)
    auth_required = Column(Boolean, default=True)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())

class ApiEventLog(Base):
    __tablename__ = "api_event_logs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    endpoint_id = Column(Integer, ForeignKey("api_endpoints.id", ondelete="CASCADE"), nullable=False)
    request_timestamp = Column(DateTime, default=datetime.datetime.utcnow)
    request_method = Column(String(10), nullable=False)
    request_headers = Column(JSON, nullable=True)
    request_body = Column(JSON, nullable=True)
    response_timestamp = Column(DateTime, default=datetime.datetime.utcnow)
    response_status_code = Column(Integer, nullable=True)
    response_headers = Column(JSON, nullable=True)
    response_body = Column(JSON, nullable=True)
    error_message = Column(String, nullable=True)    

class ApiToken(Base):
    __tablename__ = "api_tokens"

    id = Column(Integer, primary_key=True, autoincrement=True)
    endpoint_id = Column(Integer, ForeignKey("api_endpoints.id", ondelete="CASCADE"), nullable=False)
    access_token = Column(String, nullable=False)
    token_type = Column(String, nullable=False)
    expires_in = Column(Integer, nullable=False)
    issued_at = Column(DateTime, default=datetime.datetime.utcnow)
    expires_at = Column(DateTime, nullable=False)
