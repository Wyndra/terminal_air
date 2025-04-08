import serviceAxios from "@/utils/request";
export const login = (data) => {
    return serviceAxios({
        url: "/api/auth/login",
        method: "post",
        data,
    });
}

export const logoutInService = () => {
    return serviceAxios({
        url: "/api/auth/logout",
        method: "get",
    });
}

export const loginBySmsCode = (data) => {
    return serviceAxios({
        url: "/api/auth/loginBySmsCode",
        method: "post",
        data,
    });
}

export const register = (data) => {
    return serviceAxios({
        url: "/api/auth/register",
        method: "post",
        data,
    });
}

export const verifyUserPassword = (data) => {
    return serviceAxios({
        url: "/api/auth/verifyUserPassword",
        method: "post",
        data,
    });
}

export const getUserInfo = () => {
    return serviceAxios({
        url: "/api/auth/getProfile",
        method: "get",
    });
}

export const updateUserInfo = (data) => {
    return serviceAxios({
        url: "/api/auth/updateProfile",
        method: "post",
        data,
    });
}

export const switchTwoFactorAuth = () => {
    return serviceAxios({
        url: "/api/auth/switchTwoFactorAuth",
        method: "get",
    });
}

export const getTwoFactorAuthSecretQRCode = () => {
    return serviceAxios({
        url: "/api/auth/getTwoFactorAuthSecretQRCode",
        method: "get",
    });
}

export const verifyTwoFactorAuthCode = (data) => {
    return serviceAxios({
        url: "/api/auth/verifyTwoFactorAuthCode",
        method: "post",
        data,
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("twoFactorAuthToken"),
        },
    });
}

export const loginRequireTwoFactorAuth = (data) => {
    return serviceAxios({
        url: "/api/auth/loginRequireTwoFactorAuth",
        method: "post",
        data,
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("twoFactorAuthToken"),
        },
    });
}
// 通过用户当前的token获取二次验证的token
export const getTwoFactorAuthTokenByCurrentUser = () => {
    return serviceAxios({
        url: "/api/auth/getTwoFactorAuthTokenByCurrentUser",
        method: "get",
    });
}

export const getUserAvatar = () => {
    return serviceAxios({
        url: "/api/auth/getUserAvatar",
        method: "get",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("twoFactorAuthToken"),
        },
    });
}

export const updatePassword = (data) => {
    return serviceAxios({
        url: "/api/auth/updatePassword",
        method: "post",
        data,
    });
}



