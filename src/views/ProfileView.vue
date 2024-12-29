<template>
    <n-layout style="height: 100vh; position: relative;">
        <!-- Header -->
        <n-layout-header class="header" bordered>
            <div style="display: flex; height: 100%; align-items: center">
                <span>Terminal Air</span>
                <div style="flex: 1;"></div>
                <div style="height: 100%; display: flex; align-items: center">
                    <!-- 登录按钮显示 -->
                    <n-button v-if="!InLogin" style="margin-right: 24px;" @click="openLoginModal">
                        登录
                    </n-button>

                    <!-- 登录后显示个人信息 -->
                    <n-popover v-else trigger="hover" v-if="!isLoading">
                        <template #trigger>
                            <div class="username_avatar" v-if="userInfo && userInfo.username">
                                <n-avatar id="user-avatar" round size="large"
                                    :src="userInfo.avatar || 'https://s2.loli.net/2024/08/07/1wVfdgByjev7IP6.jpg'" />
                                <span v-if="userInfo.username"
                                    style="margin-right: 24px; margin-left: 10px !important;">
                                    {{ userInfo.nickname || userInfo.username }}
                                </span>
                            </div>
                        </template>
                        <template #header>
                            <n-text @click="gotoHomeView()" depth="1">返回首页</n-text>
                        </template>
                        <template #footer>
                            <span @click="logout">退出登录</span>
                        </template>
                    </n-popover>
                </div>
            </div>
        </n-layout-header>

        <!-- Main Content -->
        <n-layout class="main-content">
            <!-- Side and Content -->
            <n-layout style="height: 100%;">
                <n-layout-content
                    content-style="padding: 0px; padding-left: 24px; padding-right: 12px; padding-top: 12px;padding-bottom: 12px;height: 100%; overflow: hidden;">
                    <n-card title="基本信息" style="width: 50%;">
                        <n-tabs type="line" animated>
                            <n-tab-pane name="个人信息">
                                <!-- 个人信息表单 -->
                                <n-form :model="userInfo" label-placement="left" label-width="100px"
                                    :rules="userInfoRules" ref="userInfoForm">
                                    <n-form-item label="头像">
                                        <div style="display: flex; flex-direction: column; gap: 8px;">
                                            <n-avatar round size="large"
                                                :src="userInfo.avatar || 'https://s2.loli.net/2024/08/07/1wVfdgByjev7IP6.jpg'" />
                                            <n-upload :action="uploadUrl" :max-size="2097152" accept="image/*"
                                                @before-upload="handleBeforeUpload" @finish="handleUploadFinish"
                                                @error="handleUploadError" :custom-request="customUpload">
                                                <n-button>更换头像</n-button>
                                            </n-upload>
                                        </div>
                                    </n-form-item>
                                    <n-form-item label="用户名" required path="username">
                                        <n-input v-model:value="userInfo.username" disabled />
                                    </n-form-item>
                                    <n-form-item label="昵称" required path="nickname">
                                        <n-input v-model:value="userInfo.nickname" />
                                    </n-form-item>
                                    <n-form-item label="手机号码" required path="phone">
                                        <n-input v-model:value="userInfo.phone" />
                                    </n-form-item>
                                    <n-form-item label="邮箱" required path="email">
                                        <n-input v-model:value="userInfo.email" />
                                    </n-form-item>

                                    <n-form-item style="display: flex; justify-content: center;">
                                        <div>
                                            <n-button type="primary" @click="handleSubmit">提交</n-button>
                                        </div>
                                    </n-form-item>
                                </n-form>
                            </n-tab-pane>

                            <!-- 安全设置 -->
                            <n-tab-pane name="安全设置">
                                <n-form :model="safeSettingForm" label-placement="left" label-width="100px"
                                    :rules="safeSettingRules" ref="safetySettingForm">
                                    <n-form-item label="本地服务">
                                        <div
                                            style="display: flex; flex-direction: column; align-items: flex-start; justify-content: flex-start;">
                                            <n-switch v-model:value="store.state.usingLocalhostWs"
                                                :disabled="!saltLockStatus" />
                                            <span style="font-size: 12px; color: #999; margin-top: 4px;">
                                                使用本地 WebSocket 服务，更安全，最放心。详情请查看我们的
                                                <a href="/user-manual" target="_blank"
                                                    style="color: #007bff; text-decoration: none;">《使用手册》</a>。
                                            </span>
                                        </div>
                                    </n-form-item>
                                    <n-form-item label="加密密钥" required path="salt">
                                        <div
                                            style="display: flex; flex-direction: column; align-items: flex-start; justify-content: flex-start;width: 56%;">
                                            <n-input type="password" show-password-on="mousedown"
                                                v-model:value="displayedSalt" :disabled="!saltLockStatus"
                                                @focus="handleSaltInputFocus" />
                                            <span style="font-size: 12px; color: #999; margin-top: 4px;">
                                                您的连接密码将通过该密钥进行加密，请务必妥善保管。
                                            </span>
                                        </div>
                                    </n-form-item>
                                    <!-- 锁定按钮 -->
                                    <div style="display: flex; justify-content: center; width: 100%;">
                                        <n-button style="font-size: 18px;" size="large" text
                                            @click="handleLockByPassword">
                                            <n-icon v-if="!saltLockStatus">
                                                <LockClosed />
                                            </n-icon>
                                            <n-icon v-else>
                                                <LockOpen />
                                            </n-icon>
                                        </n-button>
                                    </div>
                                </n-form>
                            </n-tab-pane>

                            <!-- 其他设置 -->
                            <n-tab-pane name="其他设置">
                                <n-space direction="vertical">
                                    <n-text>其他设置</n-text>
                                </n-space>
                            </n-tab-pane>
                        </n-tabs>
                    </n-card>
                </n-layout-content>
            </n-layout>
        </n-layout>

        <!-- Footer -->
        <n-layout-footer class="footer" bordered>
            <div>
                <span>© 2024 Terminal Air 慕垂科技 - 浙ICP备2023031974号</span>
            </div>
        </n-layout-footer>

        <!-- 加密密钥弹出框 -->
        <UseLockByPasswordModal v-model:show="showLockByPasswordModal"
            @unlockByPasswordEvent="handleVerifyUserPasswordResult" />

        <!-- 登录/注册模态框 -->
        <n-modal v-model:show="showLoginOrRegisterModal">
            <LoginAndRegisterModal @close="showLoginOrRegisterModal = false" />
        </n-modal>
    </n-layout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useStore } from 'vuex';
import { useMessage } from 'naive-ui';
import { getUserInfo, updateUserInfo } from '@/api/auth';
import { LockClosed, LockOpen } from '@vicons/ionicons5';
import axios from 'axios';

import LoginAndRegisterModal from '@/components/LoginAndRegisterModal.vue';
import UseLockByPasswordModal from '@/components/UseLockByPasswordModal.vue';

const store = useStore();
const message = useMessage();

const showLoginOrRegisterModal = ref(false);
const userInfo = ref({
    username: '',
    nickname: '',
    phone: '',
    email: '',
    avatar: ''
});
const safeSettingForm = ref({
    salt: ''
});
const saltLockStatus = ref(false);
const showLockByPasswordModal = ref(false);
const isShaking = ref(false);

const InLogin = ref(!!localStorage.getItem('token'));
const hasShownError = ref(store.getters.hasShownError);
const isLoading = ref(true);  // 添加一个加载状态

const displayedSalt = computed({
    get: () => {
        if (!saltLockStatus.value) {
            return '*'.repeat(safeSettingForm.value.salt?.length || 0);
        }
        return safeSettingForm.value.salt;
    },
    set: (val) => {
        if (saltLockStatus.value) {
            safeSettingForm.value.salt = val;
        }
    }
});

// 打开登录模态框
const openLoginModal = () => {
    showLoginOrRegisterModal.value = true;
};

const emailPattern = /^(([^<>()\[\]\\.,;:\s@"]+(.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

const userInfoRules = {
    username: [
        { required: true, message: '用户名不能为空', trigger: ['input', 'blur'] }
    ],
    nickname: [
        { required: true, message: '昵称不能为空', trigger: ['input', 'blur'] },
        { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: ['input', 'blur'] }
    ],
    phone: [
        { required: true, message: '手机号码不能为空', trigger: ['input', 'blur'] },
        { pattern: /^[1][3-9][0-9]{9}$/, message: '请输入有效的手机号码', trigger: ['input', 'blur'] }
    ],
    email: [
        { required: true, message: '邮箱不能为空', trigger: ['input', 'blur'] },
        { pattern: emailPattern, message: '请输入有效的邮箱地址', trigger: ['input', 'blur'] }
    ]
};

const safeSettingRules = {
    salt: [
        { required: true, message: '加密密钥不能为空', trigger: ['input', 'blur'] }
    ]
};

async function fetchUserInfo() {
    try {
        const res = await getUserInfo();
        if (res.status === '200') {
            userInfo.value = res.data;
            safeSettingForm.value = {
                salt: res.data.salt || ''
            };
        } else {
            if (!hasShownError.value) {
                message.error(res.message || '获取用户信息失败');
                store.commit('setHasShownError', true);
                hasShownError.value = true;
            }
        }
    } catch (error) {
        if (!hasShownError.value) {
            message.error('请求用户信息时出错');
            store.commit('setHasShownError', true);
            hasShownError.value = true;
        }
    } finally {
        isLoading.value = false;  // 完成加载
    }
}

const handleSubmit = async () => {
    try {
        const res = await updateUserInfo(userInfo.value);
        if (res.status === '200') {
            message.success('更新成功');
        } else {
            message.error(res.message || '更新失败');
        }
    } catch (error) {
        message.error('更新时出错');
    }
};

const handleVerifyUserPasswordResult = (isUnlocked) => {
    if (isUnlocked) {
        // 解锁成功，并关闭弹窗
        saltLockStatus.value = true;
        showLockByPasswordModal.value = false;
    } else {
        saltLockStatus.value = false;  // 解锁失败，继续显示弹窗
    }
};

const handleLockByPassword = () => {
    if (saltLockStatus.value) {
        saltLockStatus.value = false;
    } else {
        showLockByPasswordModal.value = true;
    }
    // 添加抖动效果
    isShaking.value = true;
    setTimeout(() => {
        isShaking.value = false;
    }, 500);
};

// 当加密密钥输入框获得焦点时提示用户解锁
const handleSaltInputFocus = () => {
    if (!saltLockStatus.value) {
        message.info('请先点击左侧的锁图标解锁');
        // 触发抖动效果
        isShaking.value = true;
        setTimeout(() => {
            isShaking.value = false;
        }, 500);
    }
};

const changeSaltPassword = () => {
    saltLockStatus.value = true;
};

const logout = () => {
    localStorage.removeItem('token');
    InLogin.value = false;
    location.href = '/';
    location.reload();
};

const gotoHomeView = () => {
    location.href = '/';
};

// 上传相关配置
const uploadUrl = 'http://static.srcandy.top/upload';

// 自定义上传函数
const customUpload = async ({ file, onProgress, onSuccess, onError }) => {
    if (!file || !file.file) { // 添加文件存在性检查
        message.error('未选择文件');
        onError(new Error('未选择文件'));
        return;
    }

    const formData = new FormData();
    formData.append('file', file.file, file.file.name); // 修改为 'file.file' 和 'file.file.name'

    try {
        const response = await axios.post(uploadUrl, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: (event) => {
                if (event.lengthComputable) {
                    const percent = Math.round((event.loaded * 100) / event.total);
                    onProgress({ percent });
                }
            }
        });

        if (response.status === 200) {
           console.log('上传成功:', response.data);
           userInfo.value.avatar = response.data.data.url;
        } else {
            message.error('头像上传失败');
            onError(new Error('头像上传失败'));
        }
    } catch (error) {
        console.error('上传错误:', error);
        message.error('头像上传失败');
        onError(error);
    }
};

// 上传前校验
const handleBeforeUpload = (data) => {
    if (data.file.file?.size > 2097152) {
        message.error('文件大小不能超过2MB');
        return false;
    }
    return true;
};

// 上传完成处理
const handleUploadFinish = async ({ file }) => {
    try {
        console.log('完整的 file 对象:', file); // 新增调试信息

        const response = file.response;
        console.log('上传响应:', response); // 调试信息

        if (!response) {
            console.error('响应为空');
            message.error('响应为空');
            return;
        }

        if (response.status === 200) {  // 确认 status 是数字
            // 使用 file.uid 代替 file.id
            if (!file.uid) {
                console.error('文件 UID 不存在');
                message.error('文件 UID 不存在');
                return;
            }

            // 更新头像URL
            userInfo.value.avatar = response.data.url;
            
            // 更新到服务器
            const updateRes = await updateUserInfo({
                ...userInfo.value,
                avatar: response.data.url
            });
            
            if (updateRes.status === 200) {  // 修改为数字
                message.success('头像更新成功');
            } else {
                message.error('头像更新失败');
            }
        } else {
            message.error('头像上传失败');
        }
    } catch (error) {
        message.error('头像处理失败: ' + error.message);
        console.error('Upload error:', error);
    }
};

// 上传错误处理
const handleUploadError = () => {
    message.error('头像上传失败');
};

onMounted(() => {
    if (InLogin.value) {
        fetchUserInfo();
    }
});
</script>

<style scoped lang="less">
.header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 10;
    height: 60px;

    span {
        font-size: 20px;
        font-weight: bold;
        margin-left: 24px;
        align-content: center;
        font-family: ui-sans-serif, -apple-system, system-ui;
    }
}

.footer {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    text-align: center;
    height: 40px;

    div {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;

        span {
            font-size: 14px;
            font-family: ui-sans-serif, -apple-system, system-ui;
        }
    }
}
.username_avatar {
    display: flex;
align-items: center;
cursor: pointer;

span {
    font-size: 16px;
    font-weight: normal;
    font-family: ui-sans-serif, -apple-system, system-ui;
}
}



.main-content {
    padding-top: 64px;
}

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
