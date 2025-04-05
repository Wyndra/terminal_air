import serviceAxios from "@/utils/request";
export const generatePresignUrl = (data) => {
    return serviceAxios({
        url: "/api/avatar/presigned-url",
        method: "post",
        data,
    });
}