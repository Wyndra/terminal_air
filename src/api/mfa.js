import serviceAxios from "@/utils/request";
export const getTwoFactorAuthTokenByCurrentUser = () => {
    return serviceAxios({
        url: "/api/mfa/getTwoFactorAuthTokenByCurrentUser",
        method: "get",
    });
}

export const switchTwoFactorAuth = () => {
    return serviceAxios({
        url: "/api/mfa/switchTwoFactorAuth",
        method: "get",
    });
}

export const getTwoFactorAuthSecretQRCode = () => {
    return serviceAxios({
        url: "/api/mfa/getTwoFactorAuthSecretQRCode",
        method: "get",
    });
}

export const verifyTwoFactorAuthCode = (data) => {
    return serviceAxios({
        url: "/api/mfa/verifyTwoFactorAuthCode",
        method: "post",
        data,
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("twoFactorAuthToken"),
        },
    });
}