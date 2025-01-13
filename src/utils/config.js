const serverConfig = {
    baseURL: process.env.NODE_ENV === 'production'
        ? "http://api.srcandy.top" // 生产环境
        : "http://localhost:8089", // 开发环境

    wsURL: process.env.NODE_ENV === 'production'
        ? "ws://sh.srcandy.top:8089/webssh" // 生产环境 WebSocket
        : "ws://localhost:8089/webssh", // 开发环境 WebSocket

    useTokenAuthorization: true, // 是否开启 token 认证
};

export default serverConfig;
