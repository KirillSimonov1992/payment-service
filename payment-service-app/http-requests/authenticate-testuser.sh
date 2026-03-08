curl -v -X POST http://localhost:8085/realms/iprody-lms/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=basic_client" \
  -d "client_secret=myclient-secret" \
  -d "username=testuser" \
  -d "password=testpassword"