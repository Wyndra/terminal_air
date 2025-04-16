import serviceAxios from "@/utils/request";
import { Connect } from "@vicons/carbon";
import { dataTableDark } from "naive-ui";
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

export const getBindCredentialsInstallScript = (data) => {
    return serviceAxios({
        url: "/api/credentials/get/installation/url/" + data,
        method: "get",
    });
}

export const listBoundCredentials = (connectionUuid) => {
    return serviceAxios({
        url: "/api/credentials/get/bound/" + connectionUuid,
        method: "get",
    });
}