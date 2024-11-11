import axios from "axios";
import serverConfig from "./config";
import md5 from 'js-md5';

// 创建 axios 请求实例
const serviceAxios = axios.create({
    baseURL: serverConfig.baseURL, // 基础请求地址
    timeout: 10000, // 请求超时设置
    withCredentials: false, // 跨域请求是否需要携带 cookie
});

// 全局错误处理函数
function handleGlobalError(message) {
    // 这里可以添加全局错误处理逻辑，比如使用 UI 库展示错误提示
    // console.error("全局错误处理:", message);
    console.log("全局错误处理:", message);
    // alert(message); // 这是一个简单的示例，你可以用更复杂的 UI 提示代替
}

// 创建请求拦截
serviceAxios.interceptors.request.use(
    (config) => {
        // 如果开启 token 认证
        if (serverConfig.useTokenAuthorization) {
            const token = localStorage.getItem("token");
            if (token) {
                config.headers["Authorization"] = "Bearer " + token; // 请求头携带 token
                console.log("附加 token 到请求头:", config.headers["Authorization"]); // 调试信息
            }
        }
        // 设置请求头
        if (!config.headers["content-type"]) { // 如果没有设置请求头
            if (config.method === 'post' || config.method === 'put') {
                if (config.data && config.data.password && (config.url === '/auth/login' || config.url === '/auth/register')) {
                    // 对密码进行 md5 加密
                    config.data.password = md5(config.data.password);
                }
                config.headers["content-type"] = "application/json"; // 默认类型
                config.data = JSON.stringify(config.data); // 序列化,比如表单数据
            }
        }
        console.log("请求配置", config);
        return config;
    },
    (error) => {
        console.log("请求错误", error);
        handleGlobalError("请求错误");
        return Promise.reject(error);
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
        if (data.status === '500' && data.message === 'Token已过期') {
            localStorage.removeItem("token");
            // window.location.href = "/";
            handleGlobalError("Token已过期");
            return data;
        }
        if (data.status === '500' && data.message === 'is null') {
            localStorage.removeItem("token");
            // window.location.href = "/";
            handleGlobalError("Token为空");
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

            switch (error.response.data.status) {
                case 301:
                    message = "接口重定向了！";
                    break;
                case 400:
                    message = "参数不正确！";
                    break;
                case 401:
                    message = "您未登录，或者登录已经超时，请先登录！";
                    localStorage.removeItem("token");
                    // window.location.href = "/login";
                    break;
                case 403:
                    message = "您没有权限操作！";
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
