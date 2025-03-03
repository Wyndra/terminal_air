<template>
    <n-modal v-model:show="props.lockByTotpModalVisible" class="custom-card" preset="card" title="身份验证"
        style="width:30%" :bordered="false" transform-origin="center">
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
                <VerifationCodeInput :class="{ shake: isShaking }" v-model="totpForm.totp" />
            </n-form-item>
        </div>
        <n-button type="primary" style="width: 100%;" @click="handleUnlockByTotp">
            解锁
        </n-button>
    </n-modal>
</template>
<script setup>
import { ref, defineProps, defineEmits,watch } from 'vue';
import { getTwoFactorAuthTokenByCurrentUser, verifyTwoFactorAuthCode,getUserAvatar } from '@/api/auth';
import VerifationCodeInput from './VerifationCodeInput.vue';
import { LockClosed } from '@vicons/ionicons5';
import { useMessage } from 'naive-ui';

const props = defineProps({
    lockByTotpModalVisible: Boolean, // 这里是弹窗的显示控制
});

const message = useMessage();

const userAvatar = ref(localStorage.getItem('userAvatar'));

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
        message.success('解锁成功');
        totpForm.value = { totp: '' };  // 清空密码
    } else {
        message.error('解锁失败');
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
