curl -v -X PATCH
http://localhost:8080/payments/2c4e4b42-1c3b-4b9e-9a38-7c087cd6f8f5/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED"
  }'