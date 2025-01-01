const serverConfig = {
    // baseURL: "http://localhost:8089", // 测试环境
    baseURL: "http://api.srcandy.top", // 生产环境
    // wsURL: "ws://localhost:8089/webssh", // WebSocket本地测试环境
    wsURL: "ws://sh.srcandy.top:8089/webssh", // WebSocket
    // wsLocalhostURL: "ws://localhost:8765/webssh", // WebSocket 本地测试环境
    useTokenAuthorization: true, // 是否开启 token 认证
};
export default serverConfig;