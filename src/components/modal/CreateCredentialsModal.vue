<template>
    <n-modal v-model:show="props.credentialsModalVisible" class="custom-card" preset="card" style="width:520px"
        :bordered="false" transform-origin="center" :mask-closable="false">
        <div class="card_main">
            <n-icon :size="48">
                <Credentials />
            </n-icon>
            <n-text style="font-weight: bold; font-size: 18px;">创建凭证</n-text>
            <span style="width:360px" class="description-text">
                凭证是您连接并安全访问服务器的密钥，Terminal Air时刻保护您凭证的安全性和私密性。
            </span> 
            <n-form ref="credentialsFormRef" :model="credentialsForm" label-placement="top" style="width: 100% !important;margin-top: 20px" :rules="createCredentialsRules">
                <n-form-item label="凭证名称" path="name">
                    <n-input v-model:value="credentialsForm.name" placeholder="请输入凭证名称" />
                </n-form-item>
                <n-form-item label="标签" path="tags">
                    <n-dynamic-tags v-model:value="credentialsForm.tags" />
                </n-form-item>
                <n-form-item>
                    <n-button type="primary" style="width: 100%;" @click="handleCreateCredentials">
                        创建凭证
                    </n-button>
                </n-form-item>
            </n-form>
        </div>
    </n-modal>
</template>
<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { Credentials } from "@vicons/carbon"
import { useMessage } from "naive-ui";
import { generateCredentials } from '@/api/credentials';
const props = defineProps({
    credentialsModalVisible: Boolean, // 控制弹窗的显示与隐藏
});
const emit = defineEmits(["close"]);
const message = useMessage();

const credentialsFormRef = ref(null);
const credentialsForm = ref({
    name: '',
    tags: [],
});
const createCredentialsRules = {
    name: [
        { required: true, trigger: 'blur', message: '请输入凭证名称' },
        { min: 3, max: 20, trigger: 'blur', message: '凭证名称长度在 3 到 20 个字符之间' },
    ],
};

const handleCreateCredentials = async () => {
    credentialsFormRef.value.validate(async (valid) => {
        if (!valid) {
            await generateCredentials({
                name: credentialsForm.value.name,
                tags: credentialsForm.value.tags.join(','),
            }).then((res) => {
                if (res.status === '200') {
                    message.success('凭证创建成功');
                    emit('close');
                } else {
                    message.error(res.message || '创建凭证失败');
                }
            }).catch((error) => {
                message.error('请求凭证列表时出错');
            });
        } else {
            message.error('表单校验失败');
        }
    });
    
};


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