#!/bin/bash

# Registrasi semua layanan ke Consul
consul services register -http-addr="http://127.0.0.1:8500" auth_service.json
consul services register -http-addr="http://127.0.0.1:8500" idgen_service.json
consul services register -http-addr="http://127.0.0.1:8500" snapbi_get_token_service.json


echo "All services registered to Consul."
#dos2unix reg-all.sh
