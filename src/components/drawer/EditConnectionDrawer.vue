<template>
    <n-drawer v-model:show="props.showEditConnectionDrawer" :width="502" placement="right"
        content-style="background:#edf1f2">
        <n-drawer-content closable>
            <template #header>
                <span
                    style="font-size: 20px; align-content: center; font-family: ui-sans-serif, -apple-system, system-ui">
                    修改连接
                </span>
            </template>
            <n-card bordered style="background-color: #fff;">
                <n-form label-position="top" :model="connectionForm" ref="connectionFormRef"
                    :rules="connectionFormRule">
                    <n-form-item label="连接名称" path="name">
                        <n-input v-model:value="connectionForm.name" placeholder="请输入连接名称" />
                    </n-form-item>
                    <n-form-item label="主机名" path="host">
                        <n-input v-model:value="connectionForm.host" placeholder="请输入主机名" />
                    </n-form-item>
                    <n-form-item label="端口号" path="port">
                        <n-input v-model:value="connectionForm.port" placeholder="请输入端口号" />
                    </n-form-item>
                    <n-form-item label="用户名" path="username">
                        <n-input v-model:value="connectionForm.username" placeholder="请输入用户名" />
                    </n-form-item>
                    <n-form-item label="连接方式">
                        <n-radio-group v-model:value="connectionForm.method">
                            <n-radio value="password">密码</n-radio>
                            <n-radio value="key">密钥</n-radio>
                        </n-radio-group>
                    </n-form-item>
                    <n-form-item label="密码" v-if="connectionForm.method === 'password'" path="password">
                        <div style="display: flex; flex-direction: column; width: 100%;">
                            <!-- 输入框 -->
                            <n-input v-model:value="connectionForm.password" placeholder="请输入密码" type="password"
                                show-password-on="mousedown" style="width: 100%;" />
                            <!-- 说明文字 -->
                            <span style="font-size: 12px; color: #999; margin-top: 4px;">
                                此处显示加密后的密码，Terminal Air 遵守我们的
                                <a href="/privacy-policy" target="_blank"
                                    style="color: #007bff; text-decoration: none;">隐私政策</a> 。
                            </span>
                        </div>
                    </n-form-item>
                    <n-form-item label="密钥" v-else path="credential">
                        <n-select v-model:value="connectionForm.credential" placeholder="请选择密钥"
                            :options="credentialsSelectOptions" />
                    </n-form-item>

                </n-form>
                <n-button type="primary" style="width: 100%;" @click="handleSaveAndConnect">
                    保存
                </n-button>
            </n-card>
        </n-drawer-content>
    </n-drawer>
</template>

<script setup>
import { ref, defineEmits, onMounted, nextTick, watch, defineProps } from 'vue';
import { useMessage } from 'naive-ui';
import { listBoundCredentials } from '@/api/credentials';
import { edit, list } from '@/api/connection';
import { useStore } from 'vuex';

const props = defineProps({
    showEditConnectionDrawer: Boolean,
    editConnectionInfo: Object
});

const store = useStore();
const message = useMessage();
const emit = defineEmits(["refresh","close"]);

// 表单绑定数据
const connectionForm = ref({
    name: '',
    host: '',
    port: '',
    username: '',
    method: 'password',
    password: '',
    credential: ''
});

const credentialsList = ref([]);
const credentialsSelectOptions = ref([]);

// 表单校验规则
const connectionFormRule = {
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
            required: true, message: '密码不能为空', trigger: ['input', 'blur'],
            validator: (rule, value) => connectionForm.value.method === 'password' && !value ? false : true
        },
    ],
    credential: [
        {
            required: true, message: '凭据不能为空', trigger: ['input', 'blur'],
            validator: (rule, value) => connectionForm.value.method === 'key' && !value ? false : true
        },
    ]
};

const connectionFormRef = ref(null);

const loadConnectionInfo = () => {
    connectionForm.value = {
        name: props.editConnectionInfo.connectName || '',
        host: props.editConnectionInfo.connectHost || '',
        port: props.editConnectionInfo.connectPort || '',
        username: props.editConnectionInfo.connectUsername || '',
        method: props.editConnectionInfo.connectMethod === 0 ? 'password' : 'key',
        password: props.editConnectionInfo.connectPwd || '',
        credential: credentialsList.value.find(item => item.id === props.editConnectionInfo.credentialId)?.name || ''
    };

};

// 获取凭证列表
const fetchCredentialsList = async () => {
    try {
        const res = await listBoundCredentials(props.editConnectionInfo.cid);
        if (res.status === '200') {
            credentialsList.value = res.data;
            console.log('credentialsList' + credentialsList.value[0]?.id ,credentialsList.value);
            credentialsSelectOptions.value = credentialsList.value.map((item) => {
                return {
                    label: item.name,
                    value: item.uuid,
                }
            });
        } else {
            message.error(res.message || '获取凭证列表失败');
        }
    } catch (error) {
        message.error('请求凭证列表时出错');
    }
};

// 监听 Vuex 中的 editConnectionInfo 更新
// watch(() => store.state.editConnectionInfo, () => {
//     loadConnectionInfo();
// });

onMounted(async () => {
    await fetchCredentialsList(); // 获取凭证列表
    loadConnectionInfo();
});

const asyncEditConnect = async (data) => {
    const res = await edit(data);
    if (res.status === '200') {
        message.success('连接修改成功');
        emit('refresh'); // 通知父组件刷新列表
        emit('close'); // 关闭编辑连接抽屉
        loadConnectionInfo()
    } else {
        message.error(res.message || '连接修改失败');
    }
    return res;
};

const handleSaveAndConnect = () => {
    connectionFormRef.value.validate(async (errors) => {
        if (errors) {
            message.error('请检查表单填写是否完整');
            return;
        }

        if (!localStorage.getItem('token')) {
            message.error('请先登录');
            return;
        }

        console.log('connectionForm', credentialsList.value);

        const requestData = {
            cid: props.editConnectionInfo.cid,
            name: connectionForm.value.name,
            host: connectionForm.value.host,
            port: connectionForm.value.port,
            username: connectionForm.value.username,
            method: connectionForm.value.method === 'password' ? '0' : '1',
            password: connectionForm.value.password,
            credentialUUID: credentialsList.value.find(item => item.name == connectionForm.value.credential).uuid || '',
        };

        const res = await asyncEditConnect(requestData);
        if (res.status === '200') {
            store.state.showEditConnectionDrawer = false;
            // clearconnectionForm();
        }
    });
};
</script>
