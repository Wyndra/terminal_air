<template>
    <n-card :title="currentServiceType" bordered style="background-color: #fff;width: 25%;">
        <template #header-extra>
            <n-button text @click="closeModal">
                <n-icon size="20">
                    <Close />
                </n-icon>
            </n-button>
        </template>

        <!-- 登录表单 -->
        <n-form label-position="top" :model="loginForm" :rules="loginRules" v-if="currentServiceType === '登录'">
            <n-form-item label="用户名" path="username">
                <n-input v-model:value="loginForm.username" placeholder="请输入用户名" @keydown.enter.prevent />
            </n-form-item>
            <n-form-item label="密码" path="password">
                <n-input v-model:value="loginForm.password" placeholder="请输入密码" type="password"
                    show-password-on="mousedown" @keydown.enter.prevent />
            </n-form-item>
            <n-text style="float: right;cursor:pointer;" @click="handleClickRegister">立即注册</n-text>
        </n-form>

        <!-- 注册表单 -->
        <n-form label-position="top" :model="registerForm" :rules="registerRules" v-if="currentServiceType === '注册'">
            <n-form-item label="用户名" path="username">
                <n-input v-model:value="registerForm.username" placeholder="请输入用户名" />
            </n-form-item>
            <n-form-item label="密码" path="password">
                <n-input v-model:value="registerForm.password" placeholder="请输入密码" type="password"
                    show-password-on="mousedown" />
            </n-form-item>
            <n-form-item label="确认密码" path="repeatPassword">
                <n-input v-model:value="registerForm.repeatPassword" placeholder="请输入确认密码" type="password"
                    show-password-on="mousedown" />
            </n-form-item>
        </n-form>

        <template #footer>
            <n-button type="primary" style="width: 100%;" @click="handleSubmit">{{ currentServiceType === '登录' ? '登录' :
                '注册' }}</n-button>
        </template>
    </n-card>
</template>

<script setup>
import { ref, defineEmits } from 'vue';
import { useMessage } from 'naive-ui';
import { login, register } from '../api/auth';  // 确保你的 API 路径正确
import { useStore } from 'vuex';
import { Close } from '@vicons/ionicons5';

// 获取 Vuex store
const store = useStore();
const message = useMessage();

// 定义emit方法
const emit = defineEmits(['close']);

// 登录表单数据
const loginForm = ref({
    username: null,
    password: null
});

// 注册表单数据
const registerForm = ref({
    username: '',
    password: '',
    repeatPassword: ''
});

// 当前服务类型（登录/注册）
const currentServiceType = ref('登录');

// 登录表单验证规则
const loginRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
};

// 注册表单验证规则
const registerRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符之间', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        {
            pattern: /^(?![0-9]+$)(?![a-zA-Z]+$)(?![^0-9a-zA-Z]+$)\S{8,20}$/,
            message: '密码长度应为 8~20 位，且至少包含数字、字母、特殊字符中的两种',
            trigger: 'blur'
        }
    ],
    repeatPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
            validator: (rule, value) => {
                if (value !== registerForm.value.password) {
                    return new Error('两次输入密码不一致');
                }
                return true;
            },
            trigger: 'blur'
        }
    ]
};

// 登录提交
async function async_login() {
    const res = await login({
        username: loginForm.value.username,
        password: loginForm.value.password
    });

    if (res.status === '200') {
        localStorage.setItem('token', res.data);
        message.success('登录成功');
        // 重置错误显示状态
        store.dispatch("resetHasShownError");
        emit('close'); // 关闭模态框
        location.reload();
    } else {
        message.error(res.message || '登录失败');
    }
}

// 注册提交
async function async_register() {
    const res = await register({
        username: registerForm.value.username,
        password: registerForm.value.password
    });

    if (res.status === '200') {
        message.success('注册成功');
        emit('close'); // 关闭模态框
    } else {
        message.error(res.message || '注册失败');
    }
}

// 切换到注册界面
const handleClickRegister = () => {
    currentServiceType.value = '注册';
};

// 关闭模态框
const closeModal = () => {
    emit('close');
};

// 统一提交处理
const handleSubmit = () => {
    if (currentServiceType.value === '登录') {
        async_login();
    } else {
        async_register();
    }
};
</script>

<style scoped lang="less">
/* 样式部分 */
</style>
