import serviceAxios from "@/utils/request";
export const sendVerificationCode = (data) => {
    return serviceAxios({
        url: "/api/sms/sendVerificationCode",
        method: "post",
        data,
    });
}

export const verifyCode = (data) => {
    return serviceAxios({
        url: "/api/sms/verifyCode",
        method: "post",
        data,
    });
}

export const sendSmsCodeByToken = () => {
    return serviceAxios({
        url: "/api/sms/sendSmsCodeByToken",
        method: "get",
    });
}