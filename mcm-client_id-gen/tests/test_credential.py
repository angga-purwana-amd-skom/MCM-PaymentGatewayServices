from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_generate_credentials():
    response = client.post("/api/v1/generate")
    assert response.status_code == 200
    data = response.json()
    assert "mcm_client_id" in data
    assert "mcm_client_secret" in data
