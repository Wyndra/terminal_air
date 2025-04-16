#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

UUID="{{UUID}}"
TOKEN="{{TOKEN}}"
ENCODED_PUBLIC_KEY="{{ENCODED_PUBLIC_KEY}}"
ENDPOINT="{{ENDPOINT}}"

STATUS_URL="$ENDPOINT/api/credentials/get/status/shortToken/$UUID"
AUTH_HEADER="Authorization: Bearer $TOKEN"

echo -e "${BLUE}Checking credential status...${NC}"
RESPONSE=$(curl -s -X POST "$STATUS_URL" -H "Content-Type: application/json" -H "$AUTH_HEADER")

if command -v jq >/dev/null 2>&1; then
    DATA=$(echo "$RESPONSE" | jq -r '.data // empty')
else
    DATA=$(echo "$RESPONSE" | grep -o '"data":[0-9]*' | awk -F':' '{print $2}')
fi

if [[ -z "$DATA" ]]; then
    echo -e "${RED}Error: Failed to retrieve credential status. Please check the API server or token validity.${NC}"
    exit 1
fi

if ! [[ "$DATA" =~ ^[0-9]+$ ]]; then
    echo -e "${RED}Error: Invalid response format: 'data=$DATA'.${NC}"
    exit 1
fi

if [[ "$DATA" -ne 0 ]]; then
    echo -e "${RED}Error: Credential already bound (data=$DATA).${NC}"
    exit 1
fi

echo -e "${GREEN}Credential status is valid. Proceeding with installation...${NC}"

OS_TYPE=$(uname)

echo -e "${BLUE}Decoding SSH public key...${NC}"
for i in {1..5}; do
    echo -n "."
    sleep 0.3
done
echo ""

if [ "$OS_TYPE" == "Darwin" ]; then
    PUBLIC_KEY=$(echo "$ENCODED_PUBLIC_KEY" | base64 -D)
else
    PUBLIC_KEY=$(echo "$ENCODED_PUBLIC_KEY" | base64 -d)
fi

if [ "$EUID" -eq 0 ]; then
    TARGET_HOME="/root"
else
    TARGET_HOME="$HOME"
fi

SSH_DIR="$TARGET_HOME/.ssh"
AUTHORIZED_KEYS="$SSH_DIR/authorized_keys"

if [ ! -d "$SSH_DIR" ]; then
    echo -e "${RED}Error: SSH directory ($SSH_DIR) does not exist.${NC}"
    exit 1
fi

if [ ! -f "$AUTHORIZED_KEYS" ]; then
    echo -e "${RED}Error: authorized_keys not found at $AUTHORIZED_KEYS.${NC}"
    exit 1
fi

echo "$PUBLIC_KEY" >> "$AUTHORIZED_KEYS"

echo -e "${GREEN}SSH key installed successfully.${NC}"

UPDATE_URL="$ENDPOINT/api/credentials/update/status/shortToken"
JSON_DATA="{\"short_token\":\"$TOKEN\",\"uuid\":\"$UUID\",\"status\":1}"
AUTH_HEADER="Authorization: Bearer $TOKEN"

echo -e "${BLUE}Updating credentials status...${NC}"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$UPDATE_URL" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d "$JSON_DATA")

if [ "$RESPONSE" -eq 200 ]; then
    echo -e "${GREEN}Credentials status updated successfully.${NC}"
else
    echo -e "${RED}Failed to update credentials status. HTTP Code: $RESPONSE.${NC}"
fi
