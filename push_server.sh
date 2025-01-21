#!/bin/bash
echo '✅ 正在打包...'
npm run build

echo '✅ 正在将dist目录上传至服务器'
scp -r dist/* root@sh:/etc/Share/terminal_air/
