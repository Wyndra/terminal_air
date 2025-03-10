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

export const switchTwoFactorAuth = () => {
    return serviceAxios({
        url: "/auth/switchTwoFactorAuth",
        method: "get",
    });
}

export const getTwoFactorAuthSecretQRCode = () => {
    return serviceAxios({
        url: "/auth/getTwoFactorAuthSecretQRCode",
        method: "get",
    });
}

export const verifyTwoFactorAuthCode = (data) => {
    return serviceAxios({
        url: "/auth/verifyTwoFactorAuthCode",
        method: "post",
        data,
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("twoFactorAuthToken"),
        },
    });
}

export const loginRequireTwoFactorAuth = (data) => {
    return serviceAxios({
        url: "/auth/loginRequireTwoFactorAuth",
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
        url: "/auth/getTwoFactorAuthTokenByCurrentUser",
        method: "get",
    });
}

export const getUserAvatar = () => {
    return serviceAxios({
        url: "/auth/getUserAvatar",
        method: "get",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("twoFactorAuthToken"),
        },
    });
}

export const verifyTurnstile = (data) => {
    return serviceAxios({
        url: "/auth/verifyTurnstile",
        method: "post",
        data,
    });
}

export const updatePassword = (data) => {
    return serviceAxios({
        url: "/auth//updatePassword",
        method: "post",
        data,
    });
}

