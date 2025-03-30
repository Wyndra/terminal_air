<template>
    <n-modal v-model:show="props.bindCredentialsModalVisible" class="custom-card" preset="card" style="width:520px"
        :bordered="false" transform-origin="center" :mask-closable="false">
        <div class="card_main">
            <n-icon :size="48">
                <IosLink />
            </n-icon>
            <n-text style="font-weight: bold; font-size: 18px;">绑定凭证</n-text>
            <span style="width:360px" class="description-text">
                将凭证绑定到服务器上，以便安全访问。请确保凭证的安全性，避免泄露给他人。
            </span>
            <n-code v-if="props.row?.publicKey" :code="String(props.row.publicKey)" :language="bash" word-wrap></n-code>
        </div>
    </n-modal>
</template>

<script setup>
import { defineProps,defineEmits } from 'vue';
import { IosLink } from "@vicons/ionicons4";
import bash from "highlight.js/lib/languages/bash";

const emit = defineEmits(["close", "refresh"]);
const props = defineProps({
    bindCredentialsModalVisible: Boolean,
    row: Object
});
</script>

<style scoped>
.n-card>.n-card-header .n-card-header__main {
    display: flex;
}

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
</style>
