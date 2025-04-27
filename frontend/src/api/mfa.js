import serviceAxios from "@/utils/request";
import { method } from "lodash";
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

export const initiateTwoFactorAuth = (data) => {
    return serviceAxios({
        url: "/api/mfa/twoFactorAuth/init",
        method: "get",
    });
}

export const enableTwoFactorAuth = (data) => {
    return serviceAxios({
        url: "/api/mfa/twoFactorAuth/enable",
        method: "post",
        data,
    });
}

export const disableTwoFactorAuth = () => {
    return serviceAxios({
        url: "/api/mfa/disableTwoFactorAuth",
        method: "get"
    })
}

export const downloadOneTimeCode = () => {
    return serviceAxios({
        url: "/api/mfa/twoFactorAuth/backup",
        method: "get",
        responseType: "blob"
    });
}

export const getTwoFactorAuthStatus = () => {
    return serviceAxios({
        url: "/api/mfa/twoFactorAuth/status",
        method: "get",
    });
}

export const getTwoFactorAuthTitle = () => {
    return serviceAxios({
        url: "/api/mfa/getTwoFactorAuthTitle",
        method: "get"
    })
}

export const verifyTwoFactorAuthCode = (data) => {
    return serviceAxios({
        url: "/api/mfa/verifyTwoFactorAuthCode",
        method: "post",
        data,
    });
}

export const verifyOneTimeBackupCode = (data) => {
    return serviceAxios({
        url: "/api/mfa/verifyOneTimeBackupCode",
        method: "post",
        data,
    });
}

export const refreshOneTimeBackupCode = () => {
    return serviceAxios({
        url: "/api/mfa/refreshOneTimeCodeBackup",
        method: "get"
    })
}