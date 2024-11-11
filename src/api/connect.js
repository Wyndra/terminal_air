import serviceAxios from "@/utils/request";
export const add = (data) => {
    return serviceAxios({
        url: "/connect/addConnect",
        method: "post",
        data,
    });
}

export const list = () => {
    return serviceAxios({
        url: "/connect/getConnect",
        method: "get",
    });
}