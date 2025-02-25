from sqlalchemy import Column, String, Text
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class SampleResponse(Base):
    __tablename__ = "snapbi_sample_responses"

    path_url = Column(String, primary_key=True)  # URL unik sebagai primary key
    sample_response = Column(Text, nullable=False)  # Simpan response dalam format JSON string
    http_code = Column(String, nullable=False)
