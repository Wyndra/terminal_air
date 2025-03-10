<template>
    <n-modal v-model:show="props.passwordChangeModalVisible" class="custom-card" preset="card" style="width:400px"
        :bordered="false" transform-origin="center" :mask-closable="false">
        <div class="card_main">
            <n-icon :size="48">
                <Password16Regular />
            </n-icon>
            <n-text style="font-weight: bold; font-size: 18px;">更改密码</n-text>
            <span style="width=200px" class="description-text">密码长度需介于 8 至 20 位之间，并且必须同时包含数字、字母及特殊字符，以确保安全性。</span> 
            <n-form ref="passwordFormRef" :model="passwordForm" label-placement="top" style="width: 100%; !important;margin-top:20px" :rules="changePasswordRules">
                <n-form-item label="输入你的当前密码" path="oldPassword">
                    <n-input v-model:value="passwordForm.oldPassword" type="password" placeholder="当前密码" />
                </n-form-item>
                <n-form-item label="输入新密码" path="newPassword">
                     <n-input v-model:value="passwordForm.newPassword" type="password" placeholder="新密码"/>
                </n-form-item>
                <n-form-item label="确认你的新密码" path="confirmPassword">
                    <n-input v-model:value="passwordForm.confirmPassword" type="password" placeholder="确认密码"/>
                </n-form-item>
                <n-form-item>
                    <n-button type="primary" style="width: 100%;" @click="handlePasswordChange">
                        更改密码
                    </n-button>
                </n-form-item>
            </n-form>
        </div>
    </n-modal>
</template>
<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue';
import { Password16Regular } from "@vicons/fluent"
import { updatePassword } from '@/api/auth';
import { useMessage } from "naive-ui";
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';

const store = useStore();
const router = useRouter();
const InLogin = ref(!!localStorage.getItem('token'));


const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem("twoFactorAuthToken")
  InLogin.value = false;
  store.dispatch('logout');
  router.push('/');
};
const passwordFormRef = ref(null);

const props = defineProps({
    passwordChangeModalVisible: Boolean, // 这里是弹窗的显示控制
});

const emit = defineEmits(["close"]);
const message = useMessage();
const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
}); 

const changePasswordRules = {
    oldPassword: [
        { required: true, message: '请输入你的当前密码', trigger: 'blur' }
    ],
    newPassword: [
        { required: true, message: '请输入你的新密码', trigger: 'blur' },
        {
            validator: (rule, value) => {
                if (value === passwordForm.value.oldPassword) {
                    message.error('新密码不能与当前密码相同');
                    return new Error('新密码不能与当前密码相同');
                }
                return true;
            },
            trigger: 'blur'
        },
        {
            pattern: /^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^0-9a-zA-Z]).{8,20}$/,
            message: '密码长度应为 8~20 位，且至少包含数字、字母、特殊字符各一个',
            trigger: 'blur'
        }
    ],
    confirmPassword: [
        { required: true, message: '请再次输入你的新密码', trigger: 'blur' },
        {
            validator: (rule, value) => {
                if (value !== passwordForm.value.newPassword) {
                    message.error('两次输入密码不一致');
                    return new Error('两次输入密码不一致');
                }
                return true;
            },
            trigger: 'blur'
        }
    ]
};
async function handlePasswordChange() {
    const { confirmPassword, ...passwordData } = passwordForm.value;
    passwordFormRef.value.validate().then(() => {
        updatePassword(passwordData).then(res => {
            if (res.status === '200') {
                message.success('密码修改成功');
                passwordForm.value = {
                    oldPassword: '',
                    newPassword: '',
                    confirmPassword: ''
                };
                // 关闭弹窗
                emit('close');
                // 执行退出登录操作
                logout();
            } else {
                message.error(res.message);
            }
        }).catch(err => {
            message.error('密码修改失败');
        });
    }).catch(() => {
        message.error('表单验证失败');
    });
}
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
    text-align: center;
}
</style>