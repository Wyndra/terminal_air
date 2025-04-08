import axios from "axios";
import serverConfig from "./config";
import store from "@/store";
import router from "@/router";

// 创建 axios 请求实例
const serviceAxios = axios.create({
    baseURL: serverConfig.baseURL, // 基础请求地址
    timeout: 10000, // 请求超时设置
    withCredentials: false, // 跨域请求是否需要携带 cookie
});

// 全局错误处理函数
function handleGlobalError(message) {
    console.log("全局错误处理:", message);
}

// 创建请求拦截
serviceAxios.interceptors.request.use(
    (config) => {
        // 如果开启 token 认证
        if (serverConfig.useTokenAuthorization) {
            const token = localStorage.getItem("token");
            const twoFactorAuthToken = localStorage.getItem("twoFactorAuthToken");
            if (twoFactorAuthToken && token) {
                config.headers["Authorization"] = "Bearer " + twoFactorAuthToken; // 请求头携带 token
            } else if (token) {
                config.headers["Authorization"] = "Bearer " + token; // 请求头携带 token
            }
        }
        if (!config.headers["content-type"]) { // 如果没有设置请求头
            if (config.method === 'post' || config.method === 'put') {
                config.headers["content-type"] = "application/json"; // 默认类型
                config.data = JSON.stringify(config.data); // 序列化,比如表单数据
            }
        }
        // console.log("请求配置", config);
        return config;
    },
    (error) => {
        // 全局错误处理
        handleGlobalError("请求错误");
    }
);

// 创建响应拦截
serviceAxios.interceptors.response.use(
    (res) => {
        let data = res.data;
        if (data.status === '200') {
            console.log("Response Successful:", data);
            return data;
        }
        if (data.status === '500' && data.message === '登录已过期，请重新登录' || data.message === '非法登录') {
            localStorage.removeItem("token");
            localStorage.removeItem("twoFactorAuthToken");
            store.dispatch("logout");
            router.push("/");
            handleGlobalError(data.message || "登录已过期，请重新登录");
            return data;
        }
        if (data.status === '500' && data.message === '用户不存在') {
            localStorage.removeItem("token");
            handleGlobalError("用户不存在");
            return data;
        }
        console.error("Response Error:", data);
        handleGlobalError(data.message || "未知错误");
        return data;
    },
    (error) => {
        let message = "";
        if (error && error.response) {
            console.log("响应错误", error.response);

            switch (error.response.status) {
                case 301:
                    message = "接口重定向了！";
                    break;
                case 400:
                    message = "参数不正确！";
                    break;
                case 401:
                    message = "未登录，请先登录";
                    localStorage.removeItem("token");
                    break;
                case 403:
                    message = "您没有权限操作！";
                    localStorage.removeItem("token");
                    break;
                case 404:
                    message = `请求地址出错: ${error.response.config.url}`;
                    break;
                case 408:
                    message = "请求超时！";
                    break;
                case 409:
                    message = "系统已存在相同数据！";
                    break;
                case 500:
                    message = "服务器内部错误！";
                    break;
                case 501:
                    message = "服务未实现！";
                    break;
                case 502:
                    message = "网关错误！";
                    break;
                case 503:
                    message = "服务不可用！";
                    break;
                case 504:
                    message = "服务暂时无法访问，请稍后再试！";
                    break;
                case 505:
                    message = "HTTP 版本不受支持！";
                    break;
                default:
                    message = "异常问题，请联系管理员！";
                    break;
            }
        } else {
            message = "连接到服务器失败！";
        }
        console.error("响应错误", message);
        handleGlobalError(message);
        return Promise.reject(error);
    }
);

export default serviceAxios;
