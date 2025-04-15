import serviceAxios from "@/utils/request";
export const add = (data) => {
    return serviceAxios({
        url: "/api/connection/add",
        method: "post",
        data,
    });
}

export const edit = (data) => {
    return serviceAxios({
        url: "/api/connection/update",
        method: "post",
        data,
    });
}

export const list = () => {
    return serviceAxios({
        url: "/api/connection/list",
        method: "get",
    });
}

export const del = (data) => {
    return serviceAxios({
        url: "/api/connection/delete/" + data,
        method: "post",
    });
}