#!/bin/bash

public_key=""

# 查找 authorized_keys 文件，排除无关目录
name=$(find /home /root -name 'authorized_keys' 2>/dev/null)

if [ -z "$name" ]; then
    echo "No authorized_keys file found."
    exit 1
fi

echo "Found authorized_keys file at: $name"
echo "Installing SSH key..."

# 追加 SSH 密钥，而不是覆盖
echo "$public_key" >> "$name"

if [ $? -ne 0 ]; then
    echo "Failed to write to authorized_keys file."
    exit 1
fi

echo "SSH key installed successfully."
echo "You can now SSH into the system using the private key."
