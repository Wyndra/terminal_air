<template>
    <n-modal v-model:show="props.lockByPasswordModalVisible" class="custom-card" preset="card" title="请输入登录密码"
        style="width:25%" :bordered="false" transform-origin="center">
        <n-form :model="passwordForm" :rules="formRules">
            <n-form-item label="密码" required path="password">
                <!-- 使用标准的 v-model 来绑定 PasswordForm.password -->
                <n-input v-model:value="passwordForm.password" placeholder="请输入密码" type="password"
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

const props = defineProps({
    lockByPasswordModalVisible: Boolean, // 这里是弹窗的显示控制
});
const emit = defineEmits(["unlockByPasswordEvent"]);

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
    console.log('输入的密码:', passwordForm.value.password);  // 调试信息，查看密码是否为空

    // 调用 API 验证密码，使用 await 等待请求完成
    const result = await verifyPassword({
        password: passwordForm.value.password  // 使用 PasswordForm.value 来访问实际数据
    });

    if (result && result.data) {
        emit('unlockByPasswordEvent', true);  // 密码验证成功，发出解锁成功事件
        passwordForm.value = { password: '' };  // 清空密码
    } else {
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
