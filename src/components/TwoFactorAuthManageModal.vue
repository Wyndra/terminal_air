<template>
    <n-modal v-model:show="props.twoFactorAuthManageModalVisible" class="custom-card" preset="card" style="width:400px"
        :bordered="false" transform-origin="center" :mask-closable="false">
        <template #header>
            <n-button text @click="emit('close')">
                <n-icon :size="22">
                    <ArrowBack />
                </n-icon>
            </n-button>
        </template>
        <div class="card_main">
            <n-icon :size="48">
                <LockAccess />
            </n-icon>
            <n-text style="font-weight: bold; font-size: 18px;">在身份验证工具中扫描二维码</n-text>
            <span class="description-text">并输入一次性验证码以完成设置</span>
            <div class="qr-container">
                <img class="qr-image" v-if="qrcodeImage" :src="qrcodeImage" alt="二维码" />
                <n-skeleton v-else style="width: 200px;height: 200px;" :repeat="1"></n-skeleton>
            </div>
            <div>
                <span class="description-text" style="align-items: start;font-weight: 500;">一次性验证码</span>
                <VerifationCodeInput style="margin-top: 8px;margin-bottom: 20px;" height="60px"
                    :class="{ shake: isShaking }" v-model="totpForm.totp" />
                <n-button type="primary" style="width: 100%;" @click="handleTwoFactorAuthVerify">
                    继续
                </n-button>
                <n-button quaternary style="width: 100%;margin-top: 10px;" color="#7f7e7a" @click="handleQRCodeIssue">
                    无法扫描代码？
                </n-button>
            </div>
        </div>
    </n-modal>
</template>
<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue';
import { ArrowBack } from "@vicons/ionicons5"
import { LockAccess } from '@vicons/tabler';
import { getTwoFactorAuthSecretQRCode, getTwoFactorAuthTokenByCurrentUser, verifyTwoFactorAuthCode } from '@/api/mfa';
import { useMessage } from "naive-ui";
import VerifationCodeInput from './VerifationCodeInput.vue';


const props = defineProps({
    twoFactorAuthManageModalVisible: Boolean, // 这里是弹窗的显示控制
});
const message = useMessage();

const emit = defineEmits(["close","twoFactorAuthResultEvent"]);

const qrcodeImage = ref("");

const totpForm = ref({
    totp: ''  // 绑定的密码字段
});

const handleQRCodeIssue = () => {
    message.warning('很抱歉，由于技术限制。请使用身份验证器扫描二维码。');
};

// 获取二维码
const fetchQRCode = async () => {
    try {
        const res = await getTwoFactorAuthSecretQRCode();
        if (res.status === '200') {
            setTimeout(() => {
                qrcodeImage.value = res.data;
            }, 1000)
        } else {
            message.error('获取二维码失败');
        }
    } catch (error) {
        message.error('获取二维码失败');
    }
};

const getTwoFactorToken = async () => {
    try {
        const result = await getTwoFactorAuthTokenByCurrentUser();
        // 因为当前用户已经登录，因此可以直接获取到当前用户的两步验证密钥
        localStorage.setItem('twoFactorAuthToken', result.data);
        return result;  // 返回验证结果
    } catch (error) {
        return null;  // 如果请求失败，返回 null
    }
}

const handleTwoFactorAuthVerify = async () => {
    const tokenResult = await getTwoFactorToken();
    if (!tokenResult) {
        emit('twoFactorAuthResultEvent', false);
        return;
    }
    // 调用 API 验证密码，使用 await 等待请求完成
    const result = await verifyTwoFactorAuthCode({
        code: Number(totpForm.value.totp),
        time: Number(new Date().getTime())
    });

    if (result && result.data) {
        emit('twoFactorAuthResultEvent', true);  // 密码验证成功，发出解锁成功事件
        message.success('验证成功');
        totpForm.value = { totp: '' };  // 清空密码
    } else {
        message.error('验证失败');
        emit('twoFactorAuthResultEvent', false);  // 密码验证失败，发出解锁失败事件
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

onMounted(() => {
    fetchQRCode()
})
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
}

.qr-container {
    width: 168px;
    height: 168px;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
}

.qr-image {
    width: 200px;
    /* 增大尺寸，使白色边缘超出可视区域 */
    height: 200px;
    object-fit: cover;
}
</style>