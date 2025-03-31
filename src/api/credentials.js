import serviceAxios from "@/utils/request";
export const listCredentials = () => {
    return serviceAxios({
        url: "/api/credentials/list",
        method: "get",
    });
}
export const generateCredentials = (data) => {
    return serviceAxios({
        url: "/api/credentials/generate",
        method: "post",
        data,
    });
}
export const deleteCredentials = (data) => {
    return serviceAxios({
        url: "/api/credentials/delete/" + data,
        method: "get",
    });
}

export const getCredentialsStatus = (params) => {
    return serviceAxios({
        url: "/api/credentials/get/status/" + params,
        method: "post",
    });
}

export const bindCredentials = (data) => {
    return serviceAxios({
        url: "/api/credentials/bind",
        method: "post",
        data,
    });
}
