<template>
    <n-card :title="currentServiceType" bordered style="background-color: #fff;width: 600px;">
        <template #header-extra>
            <n-button text @click="closeModal">
                <n-icon size="20">
                    <Close />
                </n-icon>
            </n-button>
        </template>

        <!-- 登录表单 -->
        <n-form ref="loginFormRef" label-position="top" :model="loginForm" :rules="loginRules"
            v-if="currentServiceType === '登录' && !useCodeLogin && !isTwoFactor">
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
            <div style="display: flex; gap: 8px; flex-direction: column; align-items: center;">
                <div style="position: relative;">
                    <n-avatar round :size="48" :src="userAvatar" />
                    <n-icon color="#cf3f37"
                        style="position: absolute; bottom: 4px; right: -2px; width: 16px; height: 16px; background-color: transparent;">
                        <LockClosed />
                    </n-icon>
                </div>
                <n-text style="font-weight: bold; font-size: 16px;">我们需要验证你的身份才能继续</n-text>
                <n-text style="font-size: 14px;">输入来自身份验证器的代码</n-text>
                <n-form-item path="code">
                    <VerifationCodeInput v-model="twoFactorForm.code" />
                </n-form-item>
            </div>
        </n-form>

        <!-- 验证码登录表单 -->
        <n-form ref="codeLoginFormRef" label-position="top" :model="codeLoginForm" :rules="codeLoginRules"
            v-if="currentServiceType === '登录' && useCodeLogin">
            <n-form-item ref="loginByCodeRef" label="手机号" path="phone">
                <n-input :allow-input="onlyDigitsInput" :maxlength="11" v-model:value="codeLoginForm.phone"
                    placeholder="请输入中国大陆手机号" />
            </n-form-item>
            <n-form-item label="验证码" path="verificationCode">
                <div style="display: flex; gap: 8px;">
                    <n-input :allow-input="onlyDigitsInput" :maxlength="6"
                        v-model:value="codeLoginForm.verificationCode" placeholder="请输入验证码" />
                    <n-button :disabled="isCodeButtonDisabled" @click="handleGetVerificationCode"
                        style="background-color: #319154; color: white;">
                        {{ codeButtonText }}
                    </n-button>
                </div>
            </n-form-item>
        </n-form>

        <!-- 注册表单 -->
        <n-form ref="registerFormRef" label-position="top" :model="registerForm" :rules="registerRules"
            v-if="currentServiceType === '注册'">
            <div style="display: flex; gap: 16px;">
                <n-form-item label="用户名" path="username" style="flex: 1;">
                    <n-input :allow-input="onlyEnglishWordsInput" v-model:value="registerForm.username"
                        placeholder="请输入用户名" />
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
                <n-form-item ref="registerPhoneRef" first label="手机号" path="phone" style="flex: 1;">
                    <n-input :allow-input="onlyDigitsInput" :maxlength="11" v-model:value="registerForm.phone"
                        placeholder="请输入中国大陆手机号" />
                </n-form-item>
                <n-form-item label="验证码" path="verificationCode" style="flex: 1;">
                    <div style="display: flex; gap: 8px;">
                        <n-input :allow-input="onlyDigitsInput" :maxlength="6"
                            v-model:value="registerForm.verificationCode" placeholder="请输入验证码" />
                        <n-button :disabled="isCodeButtonDisabled" @click="handleGetVerificationCode"
                            style="background-color: #319154; color: white;">
                            {{ codeButtonText }}
                        </n-button>
                    </div>
                </n-form-item>
            </div>
        </n-form>
        <div id="turnstile-widget"></div>

        <div style="display: flex; justify-content: space-between; margin-top: 16px;"
            v-if="currentServiceType === '登录' && !isTwoFactor">

            <n-text style="cursor: pointer; color: #319154; font-weight: bold;"
                @click="handleClickRegister">立即注册</n-text>

            <n-text style="cursor: pointer; color:#319154; font-weight: bold;" @click="toggleLoginMethod">
                {{ useCodeLogin ? '密码登录' : '验证码登录' }}
            </n-text>

        </div>
        <div style="display: flex; justify-content: space-between; margin-top: 16px;" v-if="currentServiceType === '注册'">
            <n-text style="cursor: pointer; color: #319154; font-weight: bold;"
                @click="currentServiceType = '登录'">返回登录</n-text>
            
        </div>
         <template #footer>
            <n-button type="primary" style="width: 100%;" @click="handleSubmit">{{ currentServiceType === '登录' ?
                isTwoFactor ? '继续' : '登录' : '注册' }}</n-button>
</template>
    </n-card>
</template>

<script setup>
import { ref, computed, defineEmits, nextTick, onMounted, watch } from 'vue';
import { useMessage } from 'naive-ui';
import { login, register, loginBySmsCode, loginRequireTwoFactorAuth, getUserAvatar, verifyTurnstile } from '../api/auth';  // 确保你的 API 路径正确
import { sendVerificationCode } from '../api/sms';
import { useStore } from 'vuex';
import { Close, LockClosed } from '@vicons/ionicons5';
import VerifationCodeInput from './VerifationCodeInput.vue';
import serverConfig from "@/utils/config";


const turnstileToken = ref("");

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

const refreshTurnstile = () => {
    clearTurnstile()
    nextTick(() => {
        // clearTurnstile()
        turnstile.render("#turnstile-widget", {
            sitekey: serverConfig.turnstile_siteKey,
            callback: (token) => {
                turnstileToken.value = token;
                console.log(token);
            },
            "error-callback": (error) => {
                message.error('人机验证未通过');

            },
        });
    });
}

const clearTurnstile = () => {
    turnstileToken.value = "";
    document.getElementById("turnstile-widget").innerHTML = "";
}

onMounted(() => {
    // 获取turnstileToken
    clearTurnstile()
    refreshTurnstile();
});

/*
    * 限制只能输入数字
    * @param {String} value 输入值
    * @returns {Boolean} 是否只包含数字
*/
const onlyDigitsInput = (value) => {
    return /^\d*$/.test(value);
};

/*
    * 限制输入不包括中文字符的英文字符
    * @param {String} value 输入值
    * @returns {Boolean} 是否只包含英文字符（不包括中文字符）
*/
const onlyEnglishWordsInput = (value) => {
    return /^[^\u4e00-\u9fa5]*$/.test(value);
};

// 当前服务类型（登录/注册）
const currentServiceType = ref('登录');

// 是否使用验证码登录
const useCodeLogin = ref(false);
// 是否需要二次验证
const isTwoFactor = ref(false);

// 表单引用
const loginFormRef = ref(null);
const codeLoginFormRef = ref(null);
const registerFormRef = ref(null);
const twoFactorFormRef = ref(null);
const registerPhoneRef = ref(null);
const loginByCodeRef = ref(null);

// 获取验证码按钮状态
const isCodeButtonDisabled = ref(false);
const codeButtonText = ref('获取验证码');

const userAvatar = ref('');

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
        { pattern: /^[1][3-9][0-9]{9}$/, message: '请输入有效的手机号码', trigger: ['blur','verify-phone'] }
    ],
    verificationCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
    ]
};

const twoFactorAuthRules = {
    code: [
        { required: true, message: '请输入一次性验证码', trigger: 'blur' },
        { length: 6, message: '请输入正确的一次性验证码', trigger: 'blur' }
    ]
}


// 登录提交
async function async_login() {
    const res = await login({
        username: loginForm.value.username,
        password: loginForm.value.password
    });
    if (res.status === '200') {
        if (res.data.requireTwoFactorAuth) {
            isTwoFactor.value = true;
            localStorage.setItem('twoFactorAuthToken', res.data.token);
            userAvatar.value = await getUserAvatar().then(res => res.data || '');
            message.warning("账户已启用双重认证，请输入一次性验证码")
            return;
        } else {
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
        message.success('登录成功');
        localStorage.setItem('token', res.data);
        // 移除twoFactorAuthToken
        localStorage.removeItem("twoFactorAuthToken")
        // 重置错误显示状态
        store.dispatch("resetHasShownError");
        emit('close'); // 关闭模态框
        location.reload();
    } else {
        message.error(res.message || '登录失败');
        twoFactorFormRef.value.code = '';
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
        currentServiceType.value = '登录';
        // emit('close');
    } else {
        message.error(res.message || '注册失败');
    }
}

async function async_verifyTurnstile() {
    const res = await verifyTurnstile({
        token: turnstileToken.value
    });
    // console.log(res);
    return res;
}

// 获取验证码
const handleGetVerificationCode = async () => {
    // 手动触发verifyPhoneNumber
    try {
        if (currentServiceType.value === "登录" && useCodeLogin.value) {
            await loginByCodeRef.value.validate((valid) => {
                if (!valid) {
                    message.error('请填写完整的登录信息');
                    throw new Error('Validation failed');
                }
            });
        } else if (currentServiceType.value === "注册") {
            await registerPhoneRef.value.validate((valid) => {
                if (!valid) {
                    message.error('请填写完整的注册信息');
                    throw new Error('Validation failed');
                }
            });
        }

        const channel = currentServiceType.value === '登录' ? '1008' : '1021';
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
        if (error.message !== 'Validation failed') {
            message.error('获取验证码时出错');
        }
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
    // 重置表单数据和验证状态
    await nextTick();
    if (useCodeLogin.value) {
        codeLoginFormRef.value?.resetFields?.();
    } else {
        loginFormRef.value?.resetFields?.();
    }
};

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
        if (useCodeLogin.value) {
            // 验证码登录
            codeLoginFormRef.value.validate((valid) => {
                async_verifyTurnstile().then((res) => {
                    let response = res;
                    if (response.data.success) {
                        if (!valid) {
                            async_loginWithCode();
                            // 清除人机验证
                            clearTurnstile();
                        } else {
                            message.error('请填写完整的登录信息');
                        }
                    } else {
                        message.error('请完成人机验证');
                    }
                }).catch((error) => {
                    message.error('人机验证未通过');
                });
            });
        } else if (isTwoFactor.value) {
            twoFactorFormRef.value.validate((valid) => {
                if (!valid) {
                    async_twoFactor();
                    // 清除人机验证
                    clearTurnstile();
                } else {
                    message.error('请填写完整的一次性验证码');
                }
            });
        } else {
            // 普通登录
            loginFormRef.value.validate((valid) => {
                console.log(valid);
                async_verifyTurnstile().then((res) => {
                    let response = res;
                    if (response.data.success) {
                        if (!valid) {
                            async_login();
                            // 清除人机验证
                            clearTurnstile();
                        } else {
                            message.error('请填写完整的登录信息');
                        }
                    } else {
                        message.error('请完成人机验证');
                    }
                }).catch((error) => {
                    message.error('人机验证未通过');
                });

            });
        }

    } else {
        // 注册
        registerFormRef.value.validate((valid) => {
            async_verifyTurnstile().then((res) => {
                let response = res;
                if (response.data.success) {
                    if (!valid) {
                        async_register();
                        // 清除人机验证
                        clearTurnstile();
                    } else {
                        message.error('请填写完整的注册信息');
                    }
                } else {
                    message.error('请完成人机验证');
                }
            }).catch((error) => {
                message.error('人机验证未通过');
            });
        });
    }
};

watch(
    () => [currentServiceType.value, useCodeLogin.value],
    () => {
        clearTurnstile();
        refreshTurnstile();
    },
    { deep: true }
);
</script>

<style scoped lang="less">
/* 样式部分 */
n-form-item {
    margin-bottom: 12px;
}

.n-form-item-blank {
    justify-content: center !important;
}
</style>
