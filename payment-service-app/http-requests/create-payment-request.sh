curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{
    "inquiryRefId": "607ed0ea-cb8a-4ff8-a694-1213c314e111",
    "amount": "42.50",
    "currency": "USD",
    "status": "RECEIVED"
    }'