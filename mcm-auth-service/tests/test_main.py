import unittest
from fastapi.testclient import TestClient
from app.main import app

class TestAuthModule(unittest.TestCase):
    valid_token  = ""

    @classmethod
    def setUpClass(cls):
        """ Set up the TestClient for FastAPI app """
        cls.client = TestClient(app)

    def test_get_token(self):
        """ Test token generation endpoint """
        response = self.client.post(
            "http://host.docker.internal:8086/token",
            json={"mcm_client_id": "21da9959-72fa-4f12-9a8d-a9edc9d1c62f", 
                  "mcm_client_secret": "$2b$12$xdrUDJmplXy7RLVRLblFX.kCliJDOEDe/8e9weh7r8OSKVyrrTwNG"}
        )
        print("Generated Access Token:", response.status_code)
        print("response.json():", response.json())
        self.assertEqual(response.status_code, 200)
        self.assertIn("access_token", response.json())
        # Save the token for use in the next test
        self.token = response.json()["access_token"]
        TestAuthModule.valid_token = self.token
        print("Generated Access Token:", TestAuthModule.valid_token)


    def test_secure_endpoint(self):
        """ Test secured endpoint with valid Bearer token """
        if not TestAuthModule.valid_token:
            self.skipTest("Token not generated, skipping test")

        response = self.client.get(
            "http://host.docker.internal:8086/secure-data",
            headers={"Authorization": f"Bearer {TestAuthModule.valid_token}"}
        )
        self.assertEqual(response.status_code, 200)
        self.assertIn("message", response.json())

    def test_invalid_token(self):
        """ Test secured endpoint with invalid token """
        response = self.client.get(
            "http://host.docker.internal:8086/secure-data",
            headers={"Authorization": "Bearer invalid_token"}
        )
        self.assertEqual(response.status_code, 401)
        self.assertIn("detail", response.json())

if __name__ == "__main__":
    unittest.main()
