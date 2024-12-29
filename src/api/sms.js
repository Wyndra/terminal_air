import serviceAxios from "@/utils/request";
export const sendVerificationCode = (data) => {
    return serviceAxios({
        url: "/sms/sendVerificationCode",
        method: "post",
        data,
    });
}