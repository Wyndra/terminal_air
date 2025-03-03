<template>
    <n-modal v-model:show="props.lockByPasswordModalVisible" class="custom-card" preset="card" title="身份验证"
        style="width:30%" :bordered="false" transform-origin="center">
        <n-form :model="passwordForm" :rules="formRules">
            <div style="display: flex; gap: 8px; flex-direction: column; align-items: center;">
                <div style="position: relative;">
                    <n-avatar round :size="48" :src="userAvatar" />
                    <n-icon color="#cf3f37"
                        style="position: absolute; bottom: 4px; right: -2px; width: 16px; height: 16px; background-color: transparent;">
                        <LockClosed />
                    </n-icon>
                </div>
                <n-text style="font-weight: bold; font-size: 16px;">我们需要验证你的身份才能继续</n-text>
                <n-text style="font-size: 14px;">输入登录的密码</n-text>

            </div>
            <n-form-item required path="password">
                <!-- 使用标准的 v-model 来绑定 PasswordForm.password -->
                <n-input v-model:value="passwordForm.password" placeholder="请输入登录密码" type="password"
                    show-password-on="mousedown" style="width: 100%;" :class="{ shake: isShaking }" />
            </n-form-item>
            <n-button type="primary" style="width: 100%;" @click="handleUnlockByPassword">
                解锁
            </n-button>
        </n-form>
    </n-modal>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { verifyUserPassword } from '@/api/auth';
import { LockClosed } from '@vicons/ionicons5';
import { useMessage } from 'naive-ui';

const userAvatar = ref(localStorage.getItem('userAvatar'));

const props = defineProps({
    lockByPasswordModalVisible: Boolean, // 这里是弹窗的显示控制
});
const emit = defineEmits(["unlockByPasswordEvent"]);
const message = useMessage();

const passwordForm = ref({
    password: ''  // 绑定的密码字段
});
const isShaking = ref(false);

// 发送请求验证密码
async function verifyPassword(data) {
    try {
        const result = await verifyUserPassword(data);  // 假设 verifyUserPassword 是请求验证密码的 API 函数
        return result;  // 返回验证结果
    } catch (error) {
        return null;  // 如果请求失败，返回 null
    }
}

// 点击解锁按钮时调用
const handleUnlockByPassword = async () => {  // 注意加上 async
    // 调用 API 验证密码，使用 await 等待请求完成
    const result = await verifyPassword({
        password: passwordForm.value.password  // 使用 PasswordForm.value 来访问实际数据
    });

    if (result && result.data) {
        emit('unlockByPasswordEvent', true);  // 密码验证成功，发出解锁成功事件
        passwordForm.value = { password: '' };  // 清空密码
        message.success('解锁成功');  // 弹出成功提示
    } else {
        message.error('解锁失败');  // 弹出错误提示
        emit('unlockByPasswordEvent', false);  // 密码验证失败，发出解锁失败事件
        passwordForm.value = { password: '' };  // 清空密码
        // 触发抖动效果
        isShaking.value = true;
        setTimeout(() => {
            isShaking.value = false;
        }, 500);
    }
};

// 表单验证规则
const formRules = {
    password: [
        { required: true, message: '密码不能为空', trigger: ['input', 'blur'] },
    ]
};
</script>

<style scoped>
.shake {
    animation: shake 0.5s;
}

@keyframes shake {
    0% { transform: translateX(0); }
    25% { transform: translateX(-5px); }
    50% { transform: translateX(5px); }
    75% { transform: translateX(-5px); }
    100% { transform: translateX(0); }
}
</style>
