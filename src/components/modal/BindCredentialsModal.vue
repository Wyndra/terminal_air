<template>
    <n-modal v-model:show="bindCredentialsModalVisible" class="custom-card" preset="card" style="width:520px"
        :bordered="false" transform-origin="center" :mask-closable="false">
        <div class="card_main">
            <n-icon :size="48">
                <IosLink />
            </n-icon>
            <n-text style="font-weight: bold; font-size: 18px;">绑定凭证</n-text>
            <span style="width:260px" class="description-text">
                将凭证绑定到服务器上，以便安全访问。请确保凭证的安全性，避免泄露给他人。
            </span>
            <span style="width:360px" class="description-text">
                我们提供了一种便捷的方式，请在服务器执行以下命令:
            </span>
            <n-code :code="curlCode" language="bash" word-wrap></n-code>
        </div>
    </n-modal>

</template>

<script setup>
import { computed, onMounted, watch } from "vue";
import { IosLink } from "@vicons/ionicons4";
import serverConfig from "@/utils/config";
import hljs from "highlight.js/lib/core";
import bash from "highlight.js/lib/languages/bash";
import "highlight.js/styles/github-dark.css"; // 确保路径正确

// 注册 bash 语言
hljs.registerLanguage("bash", bash);

const props = defineProps({
    bindCredentialsModalVisible: Boolean,
    row: Object
});

const token = localStorage.getItem("token") || "";

const curlCode = computed(() => {
    if (!token || !props.row?.id) {
        return "# Error: Missing token or credential ID";
    }
    const url = `${serverConfig.baseURL}/install/install.sh?token=Bearer%20${encodeURIComponent(token)}&id=${props.row.id}`;
    return `curl -sSL "${url}" | bash`;
});
</script>

<style scoped>
.card_main {
    display: flex;
    gap: 8px;
    flex-direction: column;
    align-items: center;
}

.description-text {
    color: #7f7e7a;
    font-size: 14px;
    text-align: center;
}

/* 强制修复主题未应用的问题 */
.hljs {
    background: #0d1117;
    /* github-dark 背景色 */
    color: #c9d1d9;
    /* github-dark 前景色 */
    padding: 1em;
}
</style>