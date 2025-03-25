import serviceAxios from "@/utils/request";
export const add = (data) => {
    return serviceAxios({
        url: "/api/connect/add",
        method: "post",
        data,
    });
}

export const edit = (data) => {
    return serviceAxios({
        url: "/api/connect/update",
        method: "post",
        data,
    });
}

export const list = () => {
    return serviceAxios({
        url: "/api/connect/list",
        method: "get",
    });
}

export const del = (data) => {
    return serviceAxios({
        url: "/api/connect/deleteConnect",
        method: "post",
        data,
    });
}