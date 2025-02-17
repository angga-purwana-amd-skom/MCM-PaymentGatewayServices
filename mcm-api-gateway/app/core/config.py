# config.py
import os

# Mode Debug
DEBUG_MODE = os.getenv("DEBUG_MODE", "True").lower() == "true"

CONSUL_URL=os.getenv("CONSUL_URL", "http://127.0.0.1:8500/v1/catalog/service/")

# Konfigurasi layanan MCM Core API Auth Service (deprecated, ambil dari consul)
'''
MCM_CORE_API_AUTH_SERVICE_HOSTNAME_OR_IP = os.getenv("MCM_CORE_API_AUTH_SERVICE_HOSTNAME_OR_IP", "127.0.0.1")
MCM_CORE_API_AUTH_SERVICE_PORT = os.getenv("MCM_CORE_API_AUTH_SERVICE_PORT", "8086")

# Konfigurasi layanan MCM Core API SnapBIGetTokenByApi  (deprecated, ambil dari consul)
MCM_CORE_API_SnapBIGetToken_SERVICE_HOSTNAME_OR_IP = os.getenv("MCM_CORE_API_SnapBIGetToken_SERVICE_HOSTNAME_OR_IP", "127.0.0.1")
MCM_CORE_API_SnapBIGetToken_SERVICE_PORT = os.getenv("MCM_CORE_API_SnapBIGetToken_SERVICE_PORT", "5001")


# Konfigurasi layanan MCM Core API SnapBIGetSignature  (deprecated, ambil dari consul)
MCM_CORE_API_SnapBIGetSignature_SERVICE_HOSTNAME_OR_IP = os.getenv("MCM_CORE_API_SnapBIGetSignature_SERVICE_HOSTNAME_OR_IP", "127.0.0.1")
MCM_CORE_API_SnapBIGetSignature_SERVICE_PORT = os.getenv("MCM_CORE_API_SnapBIGetSignature_SERVICE_PORT", "5002")

# Dictionary untuk menyimpan semua konfigurasi layanan (deprecated, ambil dari consul)
MCM_SERVICE_CONFIG = {
    "auth": f"http://{MCM_CORE_API_AUTH_SERVICE_HOSTNAME_OR_IP}:{MCM_CORE_API_AUTH_SERVICE_PORT}",
    "SnapBIGetToken": f"http://{MCM_CORE_API_SnapBIGetToken_SERVICE_HOSTNAME_OR_IP}:{MCM_CORE_API_SnapBIGetToken_SERVICE_PORT}",
    "SnapBIGetSignature": f"http://{MCM_CORE_API_SnapBIGetSignature_SERVICE_HOSTNAME_OR_IP}:{MCM_CORE_API_SnapBIGetSignature_SERVICE_PORT}",
}


# Mapping layanan berdasarkan modulnya
SERVICE_MAP = {
    "token": f"{MCM_SERVICE_CONFIG['auth']}/token",
    "SnapBIGetTokenByApi": f"{MCM_SERVICE_CONFIG['SnapBIGetToken']}/SnapBIGetTokenByApi",
    "SnapBIGetSignatureByApi": f"{MCM_SERVICE_CONFIG['SnapBIGetSignature']}/SnapBIGetSignatureByApi",
    "SnapBIGetBalanceInfoByApi": f"{MCM_SERVICE_CONFIG['payment']}/SnapBIGetBalanceInfoByApi",
    "SnapBIGetAccountInfoInternalByApi": f"{MCM_SERVICE_CONFIG['payment']}/SnapBIGetAccountInfoInternalByApi",
    "SnapBIGetAccountInfoExternalByApi": f"{MCM_SERVICE_CONFIG['payment']}/SnapBIGetAccountInfoExternalByApi",
    "SnapBISendFundTransferByApi": f"{MCM_SERVICE_CONFIG['transaction']}/SnapBISendFundTransferByApi",
    "SnapBIGetTransactionStatusInquiryByApi": f"{MCM_SERVICE_CONFIG['transaction']}/SnapBIGetTransactionStatusInquiryByApi",
}
'''