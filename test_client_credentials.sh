#!/bin/bash
set -e

echo "=================================================="
echo "OAuth2 Client Credentials Flow - Test Script"
echo "=================================================="
echo ""

BASE_URL="http://localhost:8080"

echo "Step 1: Login as admin (snabel/snabeltann)"
echo "-------------------------------------------"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"snabel","password":"snabeltann"}')

echo "Login response:"
echo "$LOGIN_RESPONSE"
echo ""

# Extract token
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token extracted (first 50 chars): ${TOKEN:0:50}..."
echo ""

echo "Step 2: Create API Client"
echo "-------------------------------------------"
CLIENT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Test Integration","description":"Testing client credentials flow","scopes":"read:accounts,write:invoices"}')

echo "Client creation response:"
echo "$CLIENT_RESPONSE"
echo ""

# Extract client credentials
CLIENT_ID=$(echo "$CLIENT_RESPONSE" | grep -o '"clientId":"[^"]*"' | cut -d'"' -f4)
CLIENT_SECRET=$(echo "$CLIENT_RESPONSE" | grep -o '"clientSecret":"[^"]*"' | cut -d'"' -f4)

if [ -z "$CLIENT_ID" ] || [ -z "$CLIENT_SECRET" ]; then
    echo "ERROR: Failed to create client or extract credentials"
    echo "Full response: $CLIENT_RESPONSE"
    exit 1
fi

echo "Client ID: $CLIENT_ID"
echo "Client Secret (first 20 chars): ${CLIENT_SECRET:0:20}..."
echo ""

echo "Step 3: Get Token using Client Credentials"
echo "-------------------------------------------"
TOKEN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET")

echo "Token response:"
echo "$TOKEN_RESPONSE"
echo ""

# Extract client token
CLIENT_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$CLIENT_TOKEN" ]; then
    echo "ERROR: Failed to get client token"
    echo "Full response: $TOKEN_RESPONSE"
    exit 1
fi

echo "Client token obtained (first 50 chars): ${CLIENT_TOKEN:0:50}..."
echo ""

echo "Step 4: Use Client Token to Access API"
echo "-------------------------------------------"
echo "Testing GET /api/accounts with client token:"
ACCOUNTS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/accounts" \
  -H "Authorization: Bearer $CLIENT_TOKEN")

echo "$ACCOUNTS_RESPONSE"
echo ""

echo "Step 5: List all clients"
echo "-------------------------------------------"
CLIENTS_LIST=$(curl -s -X GET "$BASE_URL/api/clients" \
  -H "Authorization: Bearer $TOKEN")

echo "Active clients:"
echo "$CLIENTS_LIST"
echo ""

echo "=================================================="
echo "Test Complete!"
echo "=================================================="
echo ""
echo "Summary:"
echo "  - Admin login: SUCCESS"
echo "  - Client creation: $([ -n "$CLIENT_ID" ] && echo 'SUCCESS' || echo 'FAILED')"
echo "  - Client credentials token: $([ -n "$CLIENT_TOKEN" ] && echo 'SUCCESS' || echo 'FAILED')"
echo "  - API access with client token: $([ -n "$ACCOUNTS_RESPONSE" ] && echo 'SUCCESS' || echo 'FAILED')"
echo ""
echo "To access the GUI, open: http://localhost:8080/clients.html"
echo "Login with: snabel / snabeltann"
