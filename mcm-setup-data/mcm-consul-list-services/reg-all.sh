#!/bin/bash

# Registrasi semua layanan ke Consul
consul services register -http-addr="http://127.0.0.1:8500" auth_service.json
consul services register -http-addr="http://127.0.0.1:8500" idgen_service.json
consul services register -http-addr="http://127.0.0.1:8500" snapbi_get_token_service.json
consul services register -http-addr="http://127.0.0.1:8500" snapbi_get_signature_service.json
consul services register -http-addr="http://127.0.0.1:8500" snapbi_get_balance_inquiry.json
consul services register -http-addr="http://127.0.0.1:8500" snapbi_get_balance_inquiry_internal.json
consul services register -http-addr="http://127.0.0.1:8500" snapbi_get_balance_inquiry_external.json
consul services register -http-addr="http://127.0.0.1:8500" snapbi_transfer_intrabank.json
consul services register -http-addr="http://127.0.0.1:8500" snapbi_transfer_interbank.json
consul services register -http-addr="http://127.0.0.1:8500" snapbi_transfer_status.json


echo "All services registered to Consul."
#dos2unix reg-all.sh
