const serverConfig = {
    baseURL: process.env.NODE_ENV === 'production'
        ? "http://api.srcandy.top" // 生产环境
        : "http://localhost:8089", // 开发环境
    wsURL: process.env.NODE_ENV === 'production'
        ? "ws://sh.srcandy.top:8089/webssh" // 生产环境 WebSocket
        : "ws://localhost:8089/webssh", // 开发环境 WebSocket
    updateURL: 'http://static.srcandy.top/upload', // 更新地址
    useTokenAuthorization: true, // 是否开启 token 认证
    turnstile_siteKey: process.env.NODE_ENV === 'production'
        ? "0x4AAAAAAA_53OVbQQBXcr4E" : true ? "1x00000000000000000000AA" : "2x00000000000000000000AB"
};

export default serverConfig;
