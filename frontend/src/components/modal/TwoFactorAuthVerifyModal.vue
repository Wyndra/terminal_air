<template>
    <n-modal v-model:show="props.selectVerifyMethodModalVisible" class="custom-card" preset="card" style="width:400px"
        :bordered="false" transform-origin="center" :mask-closable="false">
        <div style="display: flex; gap: 8px; flex-direction: column; align-items: center;">
            <div style="position: relative;">
                <n-avatar round :size="48" :src="userAvatar" />
                <n-icon color="#cf3f37"
                    style="position: absolute; bottom: 4px; right: -2px; width: 16px; height: 16px; background-color: transparent;">
                    <LockClosed />
                </n-icon>
            </div>
            <n-text style="font-weight: bold; font-size: 16px;">
                {{ title }}
            </n-text>
            <span class="description-text">{{ subTitle }}</span>
            <div v-if="currentView == 'chose'" style="width: 100%;">
                <div class="verify_method">
                    <div class="verify_method_item" @click="currentView = 'totp'">
                        <n-icon :size="24" color="#5bab70" class="icon">
                            <MoreTimeOutlined />
                        </n-icon>
                        <div class="method_name_desc">
                            <n-text style="font-size: 16px;">{{ twoFactorAuthTitle }}</n-text>
                            <span class="description-text">使用来自身份验证器的代码</span>
                        </div>
                    </div>
                    <div class="verify_method_item" @click="currentView = 'oneTimeBackup'">
                        <n-icon :size="24" color="#5bab70" class="icon">
                            <Md123Outlined />
                        </n-icon>
                        <div class="method_name_desc">
                            <n-text style="font-size: 16px;">使用备份代码</n-text>
                            <span class="description-text">使用一次性备份代码</span>
                        </div>
                    </div>
                </div>
                <n-button quaternar style="width: 100%;margin-top: 10px;" :bordered="false">
                    需要帮助？
                </n-button>
            </div>
            <div v-if="currentView == 'totp'">
                <n-form-item path="code">
                    <VerifationCodeInput v-model="totpForm.totp" height="60px" />
                </n-form-item>
                <n-button type="primary" strong style="width:100%;" @click="handleVerifyTotpCode">
                    继续
                </n-button>
                <n-button style="width: 100%;margin-top: 10px;" quaternary @click="currentView = 'chose'"
                    color="#ababab" :bordered="false">
                    尝试其他方法
                </n-button>
            </div>
            <div v-if="currentView == 'oneTimeBackup'" style="width: 100%;">
                <n-form :model="oneTimeBackupCodeForm" :rules="oneTimeBackupCodeFormRules"
                    ref="oneTimeBackupCodeFormRef">
                    <n-form-item path="oneTimeBackupCode">
                        <n-input :allow-input="onlyUpperAndSplit"
                            v-model:value="oneTimeBackupCodeForm.oneTimeBackupCode" placeholder="0000-0000"></n-input>
                    </n-form-item>
                </n-form>
                <n-button type="primary" strong style="width:100%;" @click="handleVerifyOneTimeBackupCode">
                    继续
                </n-button>
                <n-button style="width: 100%;margin-top: 10px;" quaternary @click="currentView = 'chose'"
                    color="#ababab" :bordered="false">
                    尝试其他方法
                </n-button>
            </div>
        </div>
    </n-modal>
</template>
<script setup>
import { ref, defineProps,defineEmits, computed, onMounted } from 'vue';
// import { getUserAvatar } from '@/api/auth';

import { verifyOneTimeBackupCode, getTwoFactorAuthTitle, verifyTwoFactorAuthCode } from '@/api/mfa';
import { LockClosed } from '@vicons/ionicons5';
import VerifationCodeInput from '@/components/VerifationCodeInput.vue';
import { Md123Outlined, MoreTimeOutlined } from '@vicons/material';
import { useMessage } from 'naive-ui';
import { debounce } from 'lodash';
const message = useMessage()
const currentView = ref("chose")


const emits = defineEmits(['close', 'verifySuccess']);

const totpForm = ref({
    totp: ''
})

const title = computed(() => {
    if (currentView.value == 'chose') {
        return "验证你的身份"
    } else if (currentView.value == 'totp' || currentView.value == 'oneTimeBackup') {
        return "我们需要验证你的身份才能继续"
    }
})

const subTitle = computed(() => {
    if (currentView.value == 'chose') {
        return "选择一种方法来验证你的身份"
    } else if (currentView.value == 'totp') {
        return "输入来自 " + twoFactorAuthTitle.value + " 的代码"
    } else if (currentView.value == 'oneTimeBackup') {
        return "输入未使用的一次性备份代码"
    }
})

const oneTimeBackupCodeForm = ref({
    oneTimeBackupCode: ''
})

const oneTimeBackupCodeFormRules = {
    oneTimeBackupCode: [
        { required: true, message: '一次性备份代码不能为空', trigger: 'blur' },
        { pattern: /^[A-Z0-9]{4}-[A-Z0-9]{4}$/, message: '一次性备份代码格式不正确', trigger: 'blur' }
    ]
}

const oneTimeBackupCodeFormRef = ref(null)

const onlyUpperAndSplit = (value) => {
    // 只允许大写字母和数字，并且只能包含一个分隔符
    const pattern = /^[A-Z0-9]*(-[A-Z0-9]*)?$/;
    return pattern.test(value);
}

const props = defineProps({
    selectVerifyMethodModalVisible: Boolean, // 这里是弹窗的显示控制
});
const twoFactorAuthTitle = ref("")

const userAvatar = ref(localStorage.getItem('userAvatar'));

const handleVerifyOneTimeBackupCode = debounce(() => {
    oneTimeBackupCodeFormRef.value.validate((valid) => {
        if (!valid) {
            const res = verifyOneTimeBackupCode({
                code: oneTimeBackupCodeForm.value.oneTimeBackupCode
            });
            res.then(result => {
                console.log(result);
                if (result.data) {
                    message.success("验证成功")
                    emits('verifySuccess')
                    oneTimeBackupCodeForm.value.oneTimeBackupCode = ''
                } else {
                    message.error("验证失败，请检查一次性验证码是否有效")
                    oneTimeBackupCodeForm.value.oneTimeBackupCode = ''
                }
            })
        } else {
            console.log('表单验证失败');
        }
    });
}, 1000);

const handleVerifyTotpCode = debounce(() => {
    const res = verifyTwoFactorAuthCode({
        code: Number(totpForm.value.totp),
        time: Number(new Date().getTime())
    });
    res.then(result => {
        console.log(result);
        if (result.data) {
            emits('verifySuccess')
            message.success("验证成功")
            totpForm.value.totp = ''
        } else {
            message.error("验证失败")
            totpForm.value.totp = ''
        }
    })
}, 1000);

onMounted(async () => {
    await getTwoFactorAuthTitle().then(res => {
        twoFactorAuthTitle.value = res.data;
    })
})
</script>
<style scoped>
.verify_method_item {
    display: flex;
    flex-direction: row;
    width: 100%;
    border-radius: 8px;
    border: 1px solid #e0e0e0;
    margin-top: 10px;
    padding: 8px 0px 8px 0px;
}

.verify_method_item:hover {
    background-color: #f7f7f7;
    border: 1px solid #e0e0e0;
    cursor: pointer;
}

.icon {
    margin: 0px 12px;
}

.verify_method {
    width: 100%;
    display: flex;
    flex-direction: column;
}

.method_name_desc {
    display: flex;
    flex-direction: column;
}

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