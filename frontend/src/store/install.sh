#!/bin/bash
# MIT License
# Copyright (c) 2025 wyndra term.srcandy.top
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 传递参数
UUID="%s"
TOKEN="%s"
ENCODED_PUBLIC_KEY="%s"

# 检测操作系统类型
OS_TYPE=$(uname)

echo -e "${BLUE}Decoding SSH public key...${NC}"

# 进度条模拟解码
for i in {1..5}; do
    echo -n "."
    sleep 0.3
done
echo ""

# 解码 Base64 公钥
if [ "$OS_TYPE" == "Darwin" ]; then
    PUBLIC_KEY=$(echo "$ENCODED_PUBLIC_KEY" | base64 -D)
else
    PUBLIC_KEY=$(echo "$ENCODED_PUBLIC_KEY" | base64 -d)
fi

# 确定 SSH 目录
if [ "$EUID" -eq 0 ]; then
    TARGET_HOME="/root"
else
    TARGET_HOME="$HOME"
fi

SSH_DIR="$TARGET_HOME/.ssh"
AUTHORIZED_KEYS="$SSH_DIR/authorized_keys"

# 检查 SSH 目录是否存在
if [ ! -d "$SSH_DIR" ]; then
    echo -e "${RED}Error: SSH directory ($SSH_DIR) does not exist.${NC}"
    exit 1
fi

# 检查 authorized_keys 文件是否存在
if [ ! -f "$AUTHORIZED_KEYS" ]; then
    echo -e "${RED}Error: SSH authorized_keys file not found at $AUTHORIZED_KEYS.${NC}"
    exit 1
fi

# 追加公钥
echo "$PUBLIC_KEY" >> "$AUTHORIZED_KEYS"

echo -e "${GREEN}SSH key installed successfully.${NC}"
echo -e "${GREEN}Public key added to: ${AUTHORIZED_KEYS}${NC}"
echo -e "${GREEN}You can now SSH into the system using the private key.${NC}"

# 更新服务器 credentials 状态
UPDATE_URL="https://your-api-endpoint.com/api/credentials/update/status"
JSON_DATA="{\"uuid\":\"$UUID\",\"status\":1}"
AUTH_HEADER="Authorization: Bearer $TOKEN"

echo -e "${BLUE}Updating credentials status...${NC}"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$UPDATE_URL" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d "$JSON_DATA")

if [ "$RESPONSE" -eq 200 ]; then
    echo -e "${GREEN}Credentials status updated successfully.${NC}"
else
    echo -e "${RED}Failed to update credentials status. HTTP Code: $RESPONSE${NC}"
fi
