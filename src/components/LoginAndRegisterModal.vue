<template>
    <n-card @close='closeModal' bordered style="background-color: #fff;width: 30%;">
        <!-- <template #header-extra>
            <n-button text @click="closeModal">
                <n-icon size="20">
                    <Close />
                </n-icon>
            </n-button>
        </template> -->
        <n-tabs v-model:value="currentTab">
            <n-tab-pane name="login" tab="登录">
                <!-- 登录表单 -->
                <n-form ref="loginFormRef" label-position="top" :model="loginForm" :rules="loginRules"
                    v-if="!useCodeLogin && !isTwoFactor">
                    <n-form-item label="用户名" path="username">
                        <n-input v-model:value="loginForm.username" placeholder="请输入用户名" @keydown.enter.prevent />
                    </n-form-item>
                    <n-form-item label="密码" path="password">
                        <n-input v-model:value="loginForm.password" placeholder="请输入密码" type="password"
                            show-password-on="mousedown" @keydown.enter.prevent />
                    </n-form-item>
                </n-form>

                <!-- 二次验证表单 -->
                <n-form ref="twoFactorFormRef" label-position="top" :model="twoFactorForm" :rules="twoFactorAuthRules"
                    v-if="isTwoFactor">
                    <n-form-item label="一次性验证码" path="code">
                        <n-input :allow-input="onlyAllowNumber" v-model:value="twoFactorForm.code"
                            placeholder="请输入一次性验证码" @keydown.enter.prevent />
                    </n-form-item>
                </n-form>

                <!-- 验证码登录表单 -->
                <n-form ref="codeLoginFormRef" label-position="top" :model="codeLoginForm" :rules="codeLoginRules"
                    v-if="useCodeLogin">
                    <n-form-item label="手机号" path="phone">
                        <n-input v-model:value="codeLoginForm.phone" placeholder="请输入中国大陆手机号" />
                    </n-form-item>
                    <n-form-item label="验证码" path="verificationCode">
                        <div style="display: flex; gap: 8px;">
                            <n-input v-model:value="codeLoginForm.verificationCode" :allow-input="onlyAllowNumber"
                                placeholder="请输入验证码" />
                            <n-button :disabled="isCodeButtonDisabled" @click="handleGetVerificationCode"
                                style="background-color: #319154; color: white;">
                                {{ codeButtonText }}
                            </n-button>
                        </div>
                    </n-form-item>
                </n-form>

                <div style="display: flex; justify-content: space-between; margin-top: 16px;">
                    <n-text style="cursor: pointer; color: #319154; font-weight: bold;"
                        @click="switchToRegister">立即注册</n-text>
                    <n-text style="cursor: pointer; color:#319154; font-weight: bold;" @click="toggleLoginMethod">
                        {{ useCodeLogin ? '密码登录' : '验证码登录' }}
                    </n-text>
                </div>
            </n-tab-pane>

            <n-tab-pane name="register" tab="注册">
                <!-- 注册表单 -->
                <n-form ref="registerFormRef" label-position="top" :model="registerForm" :rules="registerRules">
                    <div style="display: flex; gap: 16px;">
                        <n-form-item label="用户名" path="username" style="flex: 1;">
                            <n-input v-model:value="registerForm.username" placeholder="请输入用户名" />
                        </n-form-item>
                    </div>
                    <div style="display: flex; gap: 16px;">
                        <n-form-item label="密码" path="password" style="flex: 1;">
                            <n-input v-model:value="registerForm.password" placeholder="请输入密码" type="password"
                                show-password-on="mousedown" />
                        </n-form-item>
                        <n-form-item label="确认密码" path="repeatPassword" style="flex: 1;">
                            <n-input v-model:value="registerForm.repeatPassword" placeholder="请输入确认密码" type="password"
                                show-password-on="mousedown" />
                        </n-form-item>
                    </div>
                    <div style="display: flex; gap: 16px;">
                        <n-form-item label="手机号" path="phone" style="flex: 1;">
                            <n-input v-model:value="registerForm.phone" placeholder="请输入中国大陆手机号" />
                        </n-form-item>
                        <n-form-item label="验证码" path="verificationCode" style="flex: 1;">
                            <div style="display: flex; gap: 8px;">
                                <n-input v-model:value="registerForm.verificationCode" :allow-input="onlyAllowNumber"
                                    placeholder="请输入验证码" />
                                <n-button :disabled="isCodeButtonDisabled" @click="handleGetVerificationCode"
                                    style="background-color: #319154; color: white;">
                                    {{ codeButtonText }}
                                </n-button>
                            </div>
                        </n-form-item>
                    </div>
                </n-form>
            </n-tab-pane>
        </n-tabs>

        <template #footer>
            <n-button type="primary" style="width: 100%;" @click="handleSubmit">{{ currentTab === 'login' ? '登录' : '注册'
                }}</n-button>
        </template>
    </n-card>
</template>

<script setup>
import { ref, computed, defineEmits, nextTick } from 'vue';
import { useMessage } from 'naive-ui';
import { login, register, loginBySmsCode,loginRequireTwoFactorAuth } from '../api/auth';  // 确保你的 API 路径正确
import { sendVerificationCode } from '../api/sms';
import { useStore } from 'vuex';
// import { Close } from '@vicons/ionicons5';

// 获取 Vuex store
const store = useStore();
const message = useMessage();

const serial = ref("");

// 定义emit方法
const emit = defineEmits(['close']);

// 登录表单数据
const loginForm = ref({
    username: '',
    password: ''
});

// 验证码登录表单数据
const codeLoginForm = ref({
    phone: '',
    verificationCode: ''
});

// 注册表单数据
const registerForm = ref({
    username: '',
    password: '',
    repeatPassword: '',
    phone: '',
    verificationCode: ''
});

const twoFactorForm = ref({
    code: '',
});

// 当前选中的标签
const currentTab = ref('login');

// 是否使用验证码登录
const useCodeLogin = ref(false);
const isTwoFactor = ref(false);

// 表单引用
const loginFormRef = ref(null);
const codeLoginFormRef = ref(null);
const registerFormRef = ref(null);
const twoFactorFormRef = ref(null);

// 获取验证码按钮状态
const isCodeButtonDisabled = ref(false);
const codeButtonText = ref('获取验证码');

// 登录表单验证规则
const loginRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
};

// 验证码登录表单验证规则
const codeLoginRules = {
    phone: [
        { required: true, message: '请输入中国大陆手机号', trigger: 'blur' },
        { pattern: /^[1][3-9][0-9]{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
    ],
    verificationCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
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
            pattern: /^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^0-9a-zA-Z]).{8,20}$/,
            message: '密码长度应为 8~20 位，且至少包含数字、字母、特殊字符各一个',
            trigger: 'blur'
        }
    ],
    repeatPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
            validator: (rule, value) => {
                if (value !== registerForm.value.password) {
                    message.error('两次输入密码不一致');
                    return new Error('两次输入密码不一致');
                }
                return true;
            },
            trigger: 'blur'
        }
    ],
    phone: [
        { required: true, message: '请输入中国大陆手机号', trigger: 'blur' },
        { pattern: /^[1][3-9][0-9]{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
    ],
    verificationCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
    ]
};

const twoFactorAuthRules = {
    code: [
        { required: true, message: '请输入一次性代码', trigger: 'blur' },
        { pattern: /^\d{6}$/, message: '请输入 6 位数字', trigger: 'blur' }
    ]
}

const onlyAllowNumber = (value) => {
    return !value || /^\d+$/.test(value);
}

// 登录提交
async function async_login() {
    const res = await login({
        username: loginForm.value.username,
        password: loginForm.value.password
    });
    console.log(res);
    if (res.status === '200') {
        if (res.data.requireTwoFactorAuth) {
            isTwoFactor.value = true;
            localStorage.setItem('twoFactorAuthToken', res.data.token);

            message.warning("账户已开启双重认证，请输入一次性验证码")
            return;
        }else{
            localStorage.setItem('token', res.data.token);
            message.success('登录成功');
        }
        // 重置错误显示状态
        store.dispatch("resetHasShownError");
        emit('close'); // 关闭模态框
        location.reload();
    } else {
        message.error(res.message || '登录失败');
    }
}

// 二次验证提交
async function async_twoFactor() {
    const res = await loginRequireTwoFactorAuth({
        // 均为数字类型
        code: Number(twoFactorForm.value.code),
        time: Number(new Date().getTime())
    });

    if (res.status === '200') {
        localStorage.setItem('token', res.data);
        message.success('登录成功');
        // 移除twoFactorAuthToken
        localStorage.removeItem("twoFactorAuthToken")
        // 重置错误显示状态
        store.dispatch("resetHasShownError");
        emit('close'); // 关闭模态框
        location.reload();
    } else {
        message.error(res.message || '登录失败');
    }
}

// 验证码登录提交
async function async_loginWithCode() {
    const res = await loginBySmsCode({
        phone: codeLoginForm.value.phone,
        serial: serial.value,
        verificationCode: codeLoginForm.value.verificationCode
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
        password: registerForm.value.password,
        phone: registerForm.value.phone,
        verificationCode: registerForm.value.verificationCode
    });

    if (res.status === '200') {
        message.success('注册成功');
        emit('close'); // 关闭模态框
    } else {
        message.error(res.message || '注册失败');
    }
}

// 获取验证码
const handleGetVerificationCode = async () => {
    try {
        const channel = currentTab.value === 'login' ? '1008' : '1021';
        const phone = codeLoginForm.value.phone || registerForm.value.phone;
        const res = await sendVerificationCode({ phone, channel });
        if (res.status === '200') {
            message.success('验证码已发送');
            serial.value = res.data.serial;
            startCodeButtonCountdown();
        } else {
            message.error(res.message || '获取验证码失败');
        }
    } catch (error) {
        message.error('获取验证码时出错');
    }
};

// 验证码按钮倒计时
const startCodeButtonCountdown = () => {
    let countdown = 60;
    isCodeButtonDisabled.value = true;
    codeButtonText.value = `${countdown}s后重新获取`;

    const interval = setInterval(() => {
        countdown--;
        if (countdown > 0) {
            codeButtonText.value = `${countdown}s后重新获取`;
        } else {
            clearInterval(interval);
            codeButtonText.value = '获取验证码';
            isCodeButtonDisabled.value = false;
        }
    }, 1000);
};

// 切换登录方式
const toggleLoginMethod = async () => {
    useCodeLogin.value = !useCodeLogin.value;
    isTwoFactor.value = false;
    localStorage.removeItem("twoFactorAuthToken");
    // 重置表单数据和验证状态
    await nextTick();
    if (useCodeLogin.value) {
        codeLoginFormRef.value?.resetFields?.();
    } else {
        loginFormRef.value?.resetFields?.();
    }
};

// 切换到注册界面
const switchToRegister = () => {
    currentTab.value = 'register';
    isTwoFactor.value = false;
    localStorage.removeItem("twoFactorAuthToken");
};

// 关闭模态框
const closeModal = () => {
    emit('close');
};

// 统一提交处理
const handleSubmit = () => {
    if (currentTab.value === 'login') {
        if (useCodeLogin.value) {
            codeLoginFormRef.value.validate((valid) => {
                if (!valid) {
                    async_loginWithCode();
                } else {
                    message.error('请填写完整的登录信息');
                }
            });
        } else if (isTwoFactor.value) {
            twoFactorFormRef.value.validate((valid) => {
                if (!valid) {
                    async_twoFactor();
                } else {
                    message.error('请填写完整的一次性验证码');
                }
            });
        } else {
            loginFormRef.value.validate((valid) => {
                console.log(valid);
                if (!valid) {
                    async_login();
                } else {
                    message.error('请填写完整的登录信息');
                }
            });
        }

    } else {
        registerFormRef.value.validate((valid) => {
            if (!valid) {
                async_register();
            } else {
                message.error('请填写完整的注册信息');
            }
        });
    }
};
</script>

<style scoped lang="less">
/* 样式部分 */
n-form-item {
    margin-bottom: 12px;
}
</style>
