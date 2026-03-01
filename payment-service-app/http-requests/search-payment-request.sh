curl -v -G http://localhost:8080/payments/search \
  --data-urlencode "status=APPROVED" \
  --data-urlencode "currency=USD" \
  --data-urlencode "minAMount=10.00" \
  --data-urlencode "createdAfter=2024-01-01T00:00:00Z" \
  --data-urlencode "page=0" \
  --data-urlencode "size=10" \
  --data-urlencode "sortBy=createdAt" \
  --data-urlencode "direction=desce"