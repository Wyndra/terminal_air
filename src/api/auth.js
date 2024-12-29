import serviceAxios from "@/utils/request";
export const login = (data) => {
    return serviceAxios({
        url: "/auth/login",
        method: "post",
        data,
    });
}

export const loginBySmsCode = (data) => {
    return serviceAxios({
        url: "/auth/loginBySmsCode",
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

export const verifyUserPassword = (data) => {
    return serviceAxios({
        url: "/auth/verifyUserPassword",
        method: "post",
        data,
    });
}

export const getUserInfo = () => {
    return serviceAxios({
        url: "/auth/getProfile",
        method: "get",
    });
}

export const updateUserInfo = (data) => {
    return serviceAxios({
        url: "/auth/updateProfile",
        method: "post",
        data,
    });
}