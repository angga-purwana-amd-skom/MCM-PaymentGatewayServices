import unittest
from fastapi.testclient import TestClient
from app.main import app  # Make sure this path is correct based on your project structure

class TestClientIdGen(unittest.TestCase):
    def setUp(self):
        # Setup the test client, use FastAPI TestClient to send requests
        self.client = TestClient(app)

    def test_generate_endpoint(self):
        # Test the '/generate' endpoint to ensure it returns a valid response
        response = self.client.get("/generate")
        self.assertEqual(response.status_code, 200)  # Ensure the status code is 200 (OK)
        self.assertIn("mcm_client_id", response.json())  # Ensure the response contains 'client_id'
        self.assertIn("mcm_client_secret", response.json())

if __name__ == "__main__":
    unittest.main()
