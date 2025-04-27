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
        <!-- 关闭状态 -->
        <div v-if="!twoFactorAuthStatus">
            <div class="card_main" v-if="currentStep === 0">
                <n-icon :size="48">
                    <LockAccess />
                </n-icon>
                <n-text style="font-weight: bold; font-size: 18px;">在身份验证工具中扫描二维码</n-text>
                <span class="description-text">为你的身份验证工具命名，并输入一次性验证码以完成设置</span>
                <div class="qr-container">
                    <img class="qr-image" v-if="qrcodeImage" :src="qrcodeImage" alt="二维码" />
                    <n-skeleton v-else style="width: 200px;height: 200px;" :repeat="1"></n-skeleton>
                </div>
                <div>
                    <n-form ref="totpFormRef" :model="totpForm" label-placement="top" label-width="100px">
                        <n-form-item label="身份验证工具名称" label-width="100px">
                            <n-input v-model:value="totpForm.titleName" placeholder="例如，Authy" />
                        </n-form-item>
                        <n-form-item label="一次性验证码" label-width="100px">
                            <VerifationCodeInput height="60px" :class="{ shake: isShaking }" v-model="totpForm.totp" />
                        </n-form-item>
                    </n-form>
                    <n-button type="primary" style="width: 100%;" @click="handleTwoFactorAuthEnable">
                        继续
                    </n-button>
                    <n-button quaternary style="width: 100%;margin-top: 10px;" color="#7f7e7a"
                        @click="handleQRCodeIssue">
                        无法扫描代码？
                    </n-button>
                </div>
            </div>
            <div class="card_main" v-if="currentStep === 1">
                <n-icon :size="48">
                    <LockAccess />
                </n-icon>
                <n-text style="font-weight: bold; font-size: 18px;">你的备份代码</n-text>
                <span class="description-text">请将这些代码记录在安全的地方。这些代码只向你展示一次。</span>
                <div
                    style="width: 100%;background: #fcfaf2;display: flex;justify-content: center;border-radius: 8px;padding: 10px 0px;color: #c29343;font-weight: 550;">
                    <n-code :code="oneTimeCodeBackupCode" language="bash" word-wrap></n-code>
                </div>
                <n-button type="primary" style="width: 100%;margin-top: 10px;" @click='emit("close")'>
                    我已经记下来了
                </n-button>
                <n-button secondary style="width: 100%;margin-top: 8px;" @click="handleOneTimeCodeBackupDownload">
                    下载为文本文件
                </n-button>
            </div>
        </div>
        <div class="card_main" v-else>
            <n-icon :size="48">
                <LockAccess />
            </n-icon>
            <n-text style="font-weight: bold; font-size: 18px;">双重认证已启用</n-text>
            <span class="description-text" v-if="!refreshTrue">你可以在该界面管理双重认证</span>
            <div class="card_main" v-if="refreshTrue">
                <span class="description-text">请将这些代码记录在安全的地方。这些代码只向你展示一次。</span>
                <div
                    style="width: 100%;background: #fcfaf2;display: flex;justify-content: center;border-radius: 8px;padding: 10px 0px;color: #c29343;font-weight: 550;">
                    <n-code :code="oneTimeCodeBackupCode" language="bash" word-wrap></n-code>
                </div>
                <n-button type="primary" style="width: 100%;margin-top: 10px;" @click='emit("close")'>
                    我已经记下来了
                </n-button>
                <n-button secondary style="width: 100%;margin-top: 8px;" @click="handleOneTimeCodeBackupDownload">
                    下载为文本文件
                </n-button>
            </div>
            <div style="width:100%" v-else>
                <n-button type="primary" style="width: 100%;margin-top: 10px;" @click='handleRefreshOneTimeBackupCode'>
                    刷新一次性备份代码
                </n-button>
                <n-button secondary type="error" style="width: 100%;margin-top: 8px;"
                    @click='handleDisableTwoFactorAuth'>
                    关闭双重认证
                </n-button>
            </div>

        </div>

    </n-modal>
</template>
<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue';
import { ArrowBack } from "@vicons/ionicons5"
import { LockAccess } from '@vicons/tabler';
import { getTwoFactorAuthSecretQRCode, getTwoFactorAuthTokenByCurrentUser, verifyTwoFactorAuthCode, disableTwoFactorAuth, refreshOneTimeBackupCode, getTwoFactorAuthStatus, initiateTwoFactorAuth, enableTwoFactorAuth, downloadOneTimeCode } from '@/api/mfa';
import { useMessage, useDialog } from "naive-ui";
import VerifationCodeInput from '@/components/VerifationCodeInput.vue';
import { debounce } from 'lodash';
const refreshTrue = ref(false);


const props = defineProps({
    twoFactorAuthManageModalVisible: Boolean, // 这里是弹窗的显示控制
});
const message = useMessage();
const dialog = useDialog()

const emit = defineEmits(["close", "twoFactorAuthResultEvent"]);
const isShaking = ref(false);  // 用于控制抖动效果
const twoFactorAuthStatus = ref(false);  // 用于存储两步验证状态
const qrcodeImage = ref("");
const oneTimeCodeBackupCode = ref("");  // 用于存储备份代码
const currentStep = ref(0);  // 用于控制当前步骤

const nextStep = () => {
    currentStep.value++;
};

const totpForm = ref({
    totp: '',
    titleName: ''
});

const handleQRCodeIssue = () => {
    message.warning('很抱歉，由于技术限制。请使用身份验证器扫描二维码。');
};

const handleOneTimeCodeBackupDownload = async () => {
    downloadOneTimeCode().then(response => {
        const blob = new Blob([response.data], { type: 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = url;

        let filename = "TerminalAir Backup Code.txt";
        const contentDisposition = response.headers['content-disposition'];
        if (contentDisposition) {
            const match = contentDisposition.match(/filename="?(.+)"?/);
            if (match && match[1]) {
                filename = decodeURIComponent(match[1]);
            }
        }

        link.download = filename;
        document.body.appendChild(link);
        link.click();

        window.URL.revokeObjectURL(url);
        document.body.removeChild(link);
    }).catch(error => {
        console.error("下载失败:", error);
    });
}

// 获取二维码
const fetchQRCode = async () => {
    try {
        const res = await getTwoFactorAuthSecretQRCode();
        if (res.status === '200') {
            setTimeout(() => {
                qrcodeImage.value = res.data;
            }, 1000)
        } else {
            // message.error('获取二维码失败');
        }
    } catch (error) {
        message.error('获取二维码失败');
    }
};

const handleTwoFactorAuthEnable = async () => {
    const result = await enableTwoFactorAuth({
        title: totpForm.value.titleName,
        code: Number(totpForm.value.totp),
        time: Number(new Date().getTime())
    });
    if (result && result.data) {
        message.success('验证成功');
        oneTimeCodeBackupCode.value = result.data.oneTimeCodeBackupList.join('\n');
        console.log('备份代码:', oneTimeCodeBackupCode.value);
        totpForm.value = { totp: '', titleName: '' };  // 清空密码
        nextStep();  // 进入下一步
    } else {
        message.error('验证失败');
        totpForm.value = { totp: '', titleName: '' };  // 清空密码
        // 触发抖动效果
        isShaking.value = true;
        setTimeout(() => {
            isShaking.value = false;
        }, 500);
    }
};

const handleRefreshOneTimeBackupCode = debounce(async () => {
    const result = await refreshOneTimeBackupCode().then(res => {
        if (res.status === '200') {
            refreshTrue.value = true;
            oneTimeCodeBackupCode.value = res.data.join('\n');
            message.success('刷新成功');
        } else {
            message.error('刷新失败');
        }
    }).catch(res => {
        message.error(res.message)
    })
})

const handleDisableTwoFactorAuth = debounce(async () => {
    dialog.error({
        title: '双重验证',
        content: '你确定要关闭吗？',
        positiveText: '确定',
        negativeText: '取消',
        draggable: true,
        onPositiveClick: async () => {
            await disableTwoFactorAuth().then(res => {
                message.success("关闭成功")
            })
        },
        onNegativeClick: () => {

        }
    })
})

onMounted(async () => {
    await initiateTwoFactorAuth();
    await getTwoFactorAuthStatus()
        .then((res) => {
            if (res.status === '200') {
                twoFactorAuthStatus.value = res.data;
            } else {
                // message.error('获取状态失败');
            }
        })
        .catch((error) => {
            // message.error('获取状态失败');
        });
    if (!twoFactorAuthStatus.value) {
        await fetchQRCode();
    }
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
    width: 300px;
    text-align: center;
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

.code_style {
    width: 100%;
    background: #fcfaf2;
    display: flex;
    justify-content: center;
    border-radius: 8px;
    padding: 10px 0px;
    color: #c29343;
    font-weight: 550;
}
</style>