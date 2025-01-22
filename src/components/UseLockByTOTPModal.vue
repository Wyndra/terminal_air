<template>
    <n-modal v-model:show="props.lockByTotpModalVisible" class="custom-card" preset="card" title="请输入来自身份验证器的代码"
        style="width:25%" :bordered="false" transform-origin="center">
        <n-form :model="totpForm" :rules="formRules">
            <n-form-item label="一次性代码" required path="totp">
                <!-- 使用标准的 v-model 来绑定 PasswordForm.password -->
                <n-input v-model:value="totpForm.totp" placeholder="XXX XXX" show-password-on="mousedown"
                    style="width: 100%;" :class="{ shake: isShaking }" />
            </n-form-item>
            <n-button type="primary" style="width: 100%;" @click="handleUnlockByTotp">
                解锁
            </n-button>
        </n-form>
    </n-modal>
</template>
<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { getTwoFactorAuthTokenByCurrentUser, verifyTwoFactorAuthCode } from '@/api/auth';

const props = defineProps({
    lockByTotpModalVisible: Boolean, // 这里是弹窗的显示控制
});

const emit = defineEmits(["unlockByTotpEvent"]);
const totpForm = ref({
    totp: ''  // 绑定的密码字段
});
const isShaking = ref(false);
const getTwoFactorToken = async () => {
    try {
        const result = await getTwoFactorAuthTokenByCurrentUser();
        // 因为当前用户已经登录，因此可以直接获取到当前用户的两步验证密钥
        // console.log('当前用户的两步验证密钥:', result.data);
        localStorage.setItem('twoFactorAuthToken', result.data);
        return result;  // 返回验证结果
    } catch (error) {
        return null;  // 如果请求失败，返回 null
    }
}

// 点击解锁按钮时调用
const handleUnlockByTotp = async () => {  // 注意加上 async
    const tokenResult = await getTwoFactorToken();
    if (!tokenResult) {
        emit('unlockByTotpEvent', false);
        return;
    }
    // 调用 API 验证密码，使用 await 等待请求完成
    const result = await verifyTwoFactorAuthCode({
        code: Number(totpForm.value.totp),
        time: Number(new Date().getTime())
    });

    if (result && result.data) {
        emit('unlockByTotpEvent', true);  // 密码验证成功，发出解锁成功事件
        totpForm.value = { totp: '' };  // 清空密码
    } else {
        emit('unlockByTotpEvent', false);  // 密码验证失败，发出解锁失败事件
        totpForm.value = { totp: '' };  // 清空密码
        // 触发抖动效果
        isShaking.value = true;
        setTimeout(() => {
            isShaking.value = false;
        }, 500);
    }
    // 清除本地存储的两步验证密钥
    localStorage.removeItem('twoFactorAuthToken');
};

</script>
<style scoped>
.shake {
    animation: shake 0.5s;
}

@keyframes shake {
    0% {
        transform: translateX(0);
    }

    25% {
        transform: translateX(-5px);
    }

    50% {
        transform: translateX(5px);
    }

    75% {
        transform: translateX(-5px);
    }

    100% {
        transform: translateX(0);
    }
}
</style>
