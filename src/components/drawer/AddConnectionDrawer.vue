<template>
    <n-drawer v-model:show="store.state.showAddConnectionDrawer" :width="502" placement="right"
        content-style="background:#edf1f2">
        <n-drawer-content closable>
            <template #header>
                <span
                    style="font-size: 20px; align-content: center; font-family: ui-sans-serif, -apple-system, system-ui">
                    新增连接
                </span>
            </template>
            <n-card bordered style="background-color: #fff;">
                <n-form label-position="top" :model="connectInfoForm" ref="connectFormRef" :rules="formRules">
                    <n-form-item label="连接名称" path="name">
                        <n-input v-model:value="connectInfoForm.name" placeholder="请输入连接名称" />
                    </n-form-item>
                    <n-form-item label="主机名" path="host">
                        <n-input v-model:value="connectInfoForm.host" placeholder="请输入主机名" />
                    </n-form-item>
                    <n-form-item label="端口号" path="port">
                        <n-input v-model:value="connectInfoForm.port" placeholder="请输入端口号" />
                    </n-form-item>
                    <n-form-item label="用户名" path="username">
                        <n-input v-model:value="connectInfoForm.username" placeholder="请输入用户名" />
                    </n-form-item>
                    <n-form-item label="连接方式">
                        <n-radio-group v-model:value="connectInfoForm.method">
                            <n-radio value="password">密码</n-radio>
                            <n-radio value="key" :disabled="true">密钥</n-radio>
                        </n-radio-group>
                    </n-form-item>
                    <n-form-item label="密码" v-if="connectInfoForm.method === 'password'" path="password">
                        <div style="display: flex; flex-direction: column; width: 100%;">
                            <!-- 输入框 -->
                            <n-input v-model:value="connectInfoForm.password" placeholder="请输入密码" type="password"
                                show-password-on="mousedown" />
                            <!-- 说明文字 -->
                            <span style="font-size: 12px; color: #999; margin-top: 4px;">
                                密码将会被加密存储，Terminal Air 遵守我们的
                                <a href="/privacy-policy" target="_blank"
                                    style="color: #007bff; text-decoration: none;">隐私政策</a> 。
                            </span>
                        </div>
                    </n-form-item>
                </n-form>
                <n-button type="primary" style="width: 100%;" @click="handleSaveAndConnect">
                    保存
                </n-button>
                <span class="description-text">
                    您无法在 <span class="info">新增连接</span> 窗口中使用 SSH 密钥进行连接。您可以先不填入密码，然后前往 <a href="/profile"
                        style="color: #007bff; text-decoration: none;">凭证中心</a> 进行 SSH 密钥的绑定。
                </span>
            </n-card>

        </n-drawer-content>
    </n-drawer>
</template>

<script setup>
import { ref, defineEmits } from 'vue';
import { useMessage } from 'naive-ui';
import { add } from '@/api/connection';
import { useStore } from 'vuex';

const store = useStore();
const message = useMessage();
const emit = defineEmits(["refresh_connection_list"]);

const connectInfoForm = ref({
    name: '',
    host: '',
    port: '',
    username: '',
    method: 'password',
    password: '',
});

// 表单校验规则
const formRules = {
    name: [
        { required: true, message: '连接名称不能为空', trigger: ['input', 'blur'] },
    ],
    host: [
        { required: true, message: '主机名不能为空', trigger: ['input', 'blur'] },
    ],
    port: [
        { required: true, message: '端口号不能为空', trigger: ['input', 'blur'] },
        { pattern: /^[0-9]+$/, message: '端口号必须为数字', trigger: ['input', 'blur'] },
    ],
    username: [
        { required: true, message: '用户名不能为空', trigger: ['input', 'blur'] },
    ],
    password: [
        {
            // required: true, message: '密码不能为空', trigger: ['input', 'blur'],
            validator: (rule, value) => connectInfoForm.value.method === 'password' && !value ? false : true
        },
    ]
};

const connectFormRef = ref(null);

const clearConnectInfoForm = () => {
    connectInfoForm.value = {
        name: '',
        host: '',
        port: '',
        username: '',
        method: 'password',
        password: ''
    };
};

const asyncAddConnect = async (data) => {
    const res = await add(data);
    if (res.status === '200') {
        message.success('连接添加成功');
        emit('refresh_connection_list'); // 通知父组件刷新列表
    } else {
        message.error(res.message || '连接添加失败');
    }
    return res;
};

const handleSaveAndConnect = () => {
    connectFormRef.value.validate(async (errors) => {
        if (errors) {
            message.error('请检查表单填写是否完整');
            return;
        }

        if (!localStorage.getItem('token')) {
            message.error('请先登录');
            return;
        }

        // 转换字段值
        const data = { ...connectInfoForm.value };
        data.method = data.method === 'password' ? '0' : '1';

        const res = await asyncAddConnect(data);
        if (res.status === '200') {
            store.state.showAddConnectionDrawer = false;
            clearConnectInfoForm();
        }
    });
};
</script>
<style scoped>
.description-text {
    color: #A1A1A1;
    font-size: 14px;
    text-align: center;
    line-height: 1.5;
    font-family: ui-sans-serif,
        -apple-system,
        system-ui;

    .important {
        color: #A1A1A1;
        font-size: 14px;
        text-align: center;
        font-weight: bold;
    }

    .warning {
        color: #e4a441;
        font-size: 14px;
        text-align: center;
        font-weight: bold;
    }

    .info {
        color: #007bff;
        font-size: 14px;
        text-align: center;
    }
}
</style>
