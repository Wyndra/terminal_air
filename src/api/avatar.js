import serviceAxios from "@/utils/request";
export const generatePresignUrl = (data) => {
    return serviceAxios({
        url: "/avatar/presigned-url",
        method: "post",
        data,
    });
}