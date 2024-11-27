import serviceAxios from "@/utils/request";
export const add = (data) => {
    return serviceAxios({
        url: "/connect/addConnect",
        method: "post",
        data,
    });
}

export const edit = (data) => {
    return serviceAxios({
        url: "/connect/updateConnect",
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

export const del = (data) => {
    return serviceAxios({
        url: "/connect/deleteConnect",
        method: "post",
        data,
    });
}