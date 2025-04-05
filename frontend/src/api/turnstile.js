import serviceAxios from "@/utils/request";
export const verifyTurnstile = (data) => {
    return serviceAxios({
        url: "/api/turnstile/verify",
        method: "post",
        data,
    });
}