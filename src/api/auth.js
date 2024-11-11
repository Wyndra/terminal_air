import serviceAxios from "@/utils/request";
export const login = (data) => {
    return serviceAxios({
        url: "/auth/login",
        method: "post",
        data,
    });
}
export const register = (data) => {
    return serviceAxios({
        url: "/auth/register",
        method: "post",
        data,
    });
}

export const getUserInfo = () => {
    return serviceAxios({
        url: "/auth/getUserInfo",
        method: "get",
    });
}