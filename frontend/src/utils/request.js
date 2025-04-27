import axios from "axios";
import serverConfig from "./config";
import store from "@/store";
import router from "@/router";

// axios 实例
const serviceAxios = axios.create({
    baseURL: serverConfig.baseURL,
    timeout: 10000,
    withCredentials: false,
});

// 全局错误提示（你可以换成 message 弹窗）
function handleGlobalError(message) {
    console.warn("全局错误:", message);
}

// === 请求拦截器 ===
serviceAxios.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        const twoFactorToken = localStorage.getItem("twoFactorAuthToken");

        if (serverConfig.useTokenAuthorization && token) {
            config.headers["Authorization"] = `Bearer ${twoFactorToken || token}`;
        }

        if (!config.headers["content-type"] && ['post', 'put'].includes(config.method)) {
            config.headers["content-type"] = "application/json";
            config.data = JSON.stringify(config.data);
        }

        return config;
    },
    (error) => {
        handleGlobalError("请求发送失败");
        return Promise.reject(error);
    }
);

serviceAxios.interceptors.response.use(
    (response) => {
        if (response.config.responseType === 'blob') {
            return response;
        }

        const data = response.data;

        // 假设后端统一返回格式 { status, message, data }
        if (data.status === "200") {
            return data;
        }

        // 常见业务错误统一处理
        handleBusinessError(data);
        return Promise.reject(data);
    },
    (error) => {
        if (error.response) {
            handleHttpStatusError(error.response.status, error.response.config?.url);
        } else {
            handleGlobalError("无法连接服务器，请检查网络");
        }
        return Promise.reject(error);
    }
);

// === 业务状态码处理 ===
function handleBusinessError(data) {
    const { status, message } = data;

    switch (message) {
        case "登录已过期，请重新登录":
        case "非法登录":
            clearAuthAndRedirect();
            break;
        case "用户不存在":
            localStorage.removeItem("token");
            break;
    }

    handleGlobalError(message || "业务异常");
}

// === HTTP 状态码处理 ===
function handleHttpStatusError(status, url = "") {
    const codeMessage = {
        400: "参数不正确",
        401: "未登录，请先登录",
        403: "没有权限",
        404: `接口不存在: ${url}`,
        408: "请求超时",
        500: "服务器内部错误",
        502: "网关错误",
        503: "服务不可用",
        504: "服务暂时无法访问",
        default: "连接异常，请联系管理员",
    };

    if ([401, 403].includes(status)) {
        clearAuthAndRedirect();
    }

    handleGlobalError(codeMessage[status] || codeMessage.default);
}

// === 清除 token 并重定向到登录页 ===
function clearAuthAndRedirect() {
    localStorage.removeItem("token");
    localStorage.removeItem("twoFactorAuthToken");
    store.dispatch("logout");
    router.push("/");
}

export default serviceAxios;
