<template>
    <n-drawer v-model:show="store.state.showAddNewConnectionDrawer" :width="502" placement="right"
        content-style="background:#edf1f2">
        <n-drawer-content closable>
            <template #header>
                <span style="
          font-size: 20px;
          /* margin-left: 24px; */
          align-content: center;
          font-family: ui-sans-serif, -apple-system, system-ui">
                    新增连接
                </span>
            </template>
            <n-card bordered style="background-color: #fff;">
                <n-form label-position="top" :model="connectInfoForm">
                    <n-form-item label="连接名称">
                        <n-input v-model:value="connectInfoForm.name" placeholder="请输入连接名称" />
                    </n-form-item>
                    <n-form-item label="主机名">
                        <n-input v-model:value="connectInfoForm.host" placeholder="请输入主机名" />
                    </n-form-item>
                    <n-form-item label="端口号">
                        <n-input v-model:value="connectInfoForm.port" placeholder="请输入端口号" />
                    </n-form-item>
                    <n-form-item label="用户名">
                        <n-input v-model:value="connectInfoForm.username" placeholder="请输入用户名" />
                    </n-form-item>
                    <n-form-item label="连接方式">
                        <n-radio-group v-model:value="connectInfoForm.method">
                            <n-radio value="password">密码</n-radio>
                            <n-radio value="key">密钥</n-radio>
                        </n-radio-group>
                    </n-form-item>
                    <n-form-item label="密码" v-if="connectInfoForm.method == 'password'">
                        <n-input v-model:value="connectInfoForm.password" placeholder="请输入密码" type="password"
                            show-password-on="mousedown" />
                    </n-form-item>
                </n-form>
                <n-button type="primary" style="width: 100%;" @click="handleSaveAndConnect">保存</n-button>
            </n-card>
            <template #footer>
                <!-- <n-button>Footer</n-button> -->
                <!-- 待添加 -->
            </template>
        </n-drawer-content>
    </n-drawer>
</template>
<script setup>
import { ref } from 'vue';
import { useMessage } from 'naive-ui';
import { add } from '@/api/connect';
import { useStore } from 'vuex';


const InLogin = localStorage.getItem('token') ? true : false;

const store = useStore();
const message = useMessage();

const connectInfoForm = ref({
    name: '',
    host: '',
    port: '',
    username: '',
    method: 'password',
    password: ''
});

async function async_add_connect(data) {
    const res = await add(data);
    console.log("res", res);
    if (res.status === '200') {
        message.success('添加成功');
    } else if (res.status === '500') {
        message.error(res.message);
    } else {
        message.error('添加失败');
    }
}

const handleSaveAndConnect = () => {
    console.log('save and connect');
    store.state.showAddNewConnectionDrawer = false;
    console.log(connectInfoForm.value);
    connectInfoForm.value.method === 'password' ? connectInfoForm.value.method = '0' : connectInfoForm.value.method = '1';
    if (!InLogin) {
        message.error('请先登录');
        return;
    } else {
        async_add_connect(connectInfoForm.value);
    }

    // 保存到localStorage
    // localStorage.setItem('connectInfo', JSON.stringify(connectInfoForm.value));
};
</script>