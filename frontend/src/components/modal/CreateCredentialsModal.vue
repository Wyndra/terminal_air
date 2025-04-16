<template>
    <n-modal v-model:show="props.credentialsModalVisible" class="custom-card" preset="card" style="width:400px"
        :bordered="false" transform-origin="center" :mask-closable="false">
        <div class="card_main">
            <n-icon :size="48">
                <Credentials />
            </n-icon>
            <n-text style="font-weight: bold; font-size: 18px;">创建凭证</n-text>
            <span style="width:360px" class="description-text">
                凭证是用于连接并安全访问服务器的密钥。为降低凭证泄露的风险，我们建议为不同的服务器使用独立的凭证。每位用户最多可创建 10 个凭证。
            </span>
            <n-form ref="credentialsFormRef" :model="credentialsForm" label-placement="top"
                style="width: 100% !important;margin-top: 20px" :rules="createCredentialsRules">
                <n-form-item label="凭证名称" path="name">
                    <n-input v-model:value="credentialsForm.name" placeholder="请输入凭证名称" />
                </n-form-item>
                <n-form-item label="标签" path="tags">
                    <n-dynamic-tags v-model:value="credentialsForm.tags" />
                </n-form-item>
                <n-form-item>
                    <n-button type="primary" style="width: 100%;" :loading="createLoading" @click="handleCreateCredentials">
                        创建凭证
                    </n-button>
                </n-form-item>
            </n-form>
        </div>
    </n-modal>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { Credentials } from "@vicons/carbon";
import { useMessage } from "naive-ui";
import { generateCredentials } from '@/api/credentials';
import { debounce } from 'lodash';

const props = defineProps({
    credentialsModalVisible: Boolean, // 控制弹窗的显示与隐藏
    currentCredentialsList: Array, // 当前凭证列表
});
const emit = defineEmits(["close", "refresh"]);
const message = useMessage();
const createLoading = ref(false);

const credentialsFormRef = ref(null);
const credentialsForm = ref({
    name: '',
    tags: [],
});

const createCredentialsRules = {
    name: [
        { required: true, trigger: 'blur', message: '请输入凭证名称' },
        { min: 3, max: 20, trigger: 'blur', message: '凭证名称长度在 3 到 20 个字符之间' },
        {
            validator: (rule, value) => {
                const isDuplicate = props.currentCredentialsList.some(item => item.name === value);
                return isDuplicate ? new Error('凭证名称重复，请重新输入') : true;
            },
            trigger: 'blur'
        },
        {
            validator: (rule, value) => {
                return props.currentCredentialsList.length >= 10
                    ? new Error('凭证数量已达上限')
                    : true;
            },
            trigger: 'blur'
        }
    ],
};

// 使用 lodash 防抖处理按钮点击，防止重复提交
const handleCreateCredentials = debounce(async () => {
    credentialsFormRef.value.validate(async (valid) => {
        if (!valid) {
            createLoading.value = true;
            await generateCredentials({
                name: credentialsForm.value.name,
                tags: credentialsForm.value.tags.join('|'),
            }).then((res) => {
                if (res.status === '200') {
                    message.success('凭证创建成功');
                    emit('close');
                    emit('refresh');
                    createLoading.value = false;
                } else {
                    message.error(res.message || '创建凭证失败');
                }
            }).catch(() => {
                message.error('请求凭证列表时出错');
            });
        }
    });
}, 1000, { leading: true, trailing: false }); // 1秒内防止重复点击

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
