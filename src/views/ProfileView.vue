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
      <n-layout style="height: 100%;">
        <n-layout-content content-style="padding: 24px;">
          <n-card title="个人信息" style="max-width: 800px;">
            <n-tabs type="line" animated>
              <!-- 个人信息标签页 -->
              <n-tab-pane name="个人信息" tab="个人信息">
                <div class="profile-container">
                  <!-- 头像和基本信息区域 -->
                  <div class="profile-header">
                    <div class="avatar-section">
                      <n-avatar round :size="120"
                        :src="userInfo.avatar || 'https://s2.loli.net/2024/08/07/1wVfdgByjev7IP6.jpg'"
                        class="main-avatar" />
                      <n-upload v-if="isEditing" :action="uploadUrl" :max-size="2097152"
                        accept="image/*" @before-upload="handleBeforeUpload"
                        @finish="handleUploadFinish" @error="handleUploadError"
                        :custom-request="customUpload">
                        <n-button size="small" class="change-avatar-btn">更换头像</n-button>
                      </n-upload>
                    </div>
                    <div class="basic-info">
                      <h2>{{ userInfo.nickname || userInfo.username }}</h2>
                      <p class="user-id">ID: {{ userInfo.username }}</p>
                    </div>
                  </div>

                  <!-- 信息展示/编辑区域 -->
                  <div class="info-section">
                    <div class="section-header">
                      <h3>详细信息</h3>
                      <n-button v-if="!isEditing" secondary @click="startEditing">
                        编辑资料
                      </n-button>
                    </div>

                    <!-- 展示模式 -->
                    <template v-if="!isEditing">
                      <n-descriptions :column="2" label-align="left" label-style="width: 120px; padding-right: 16px;">
                        <n-descriptions-item label="用户名">
                          {{ userInfo.username }}
                        </n-descriptions-item>
                        <n-descriptions-item label="昵称">
                          {{ userInfo.nickname }}
                        </n-descriptions-item>
                        <n-descriptions-item label="手机号码">
                          {{ userInfo.phone }}
                        </n-descriptions-item>
                        <n-descriptions-item label="邮箱">
                          {{ userInfo.email }}
                        </n-descriptions-item>
                      </n-descriptions>
                    </template>

                    <!-- 编辑模式 -->
                    <n-form v-else :model="userInfo" :rules="userInfoRules" ref="userInfoForm">
                      <n-descriptions :column="2" label-align="left" label-style="width: 120px; padding-right: 16px;">
                        <n-descriptions-item label="用户名">
                          <n-form-item path="username">
                            <n-input v-model:value="userInfo.username" disabled />
                          </n-form-item>
                        </n-descriptions-item>
                        <n-descriptions-item label="昵称">
                          <n-form-item path="nickname">
                            <n-input v-model:value="userInfo.nickname" />
                          </n-form-item>
                        </n-descriptions-item>
                        <n-descriptions-item label="手机号码">
                          <n-form-item path="phone">
                            <n-input v-model:value="userInfo.phone" />
                          </n-form-item>
                        </n-descriptions-item>
                        <n-descriptions-item label="邮箱">
                          <n-form-item path="email">
                            <n-input v-model:value="userInfo.email" />
                          </n-form-item>
                        </n-descriptions-item>
                      </n-descriptions>
                      <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px;">
                        <n-button @click="cancelEditing">取消</n-button>
                        <n-button type="primary" @click="handleSubmit">保存</n-button>
                      </div>
                    </n-form>
                  </div>
                </div>
              </n-tab-pane>

              <!-- 安全设置标签页 -->
              <n-tab-pane name="安全设置" tab="安全设置">
                <n-descriptions :column="1" label-align="left" label-style="width: 120px; padding-right: 16px;">
                  <n-descriptions-item label="加密密钥">
                    <div class="salt-input-wrapper">
                      <n-input type="password" show-password-on="mousedown"
                        v-model:value="displayedSalt" :disabled="!saltLockStatus"
                        @focus="handleSaltInputFocus" />
                      <n-button circle size="small" @click="handleLockByPassword">
                        <template #icon>
                          <n-icon><component :is="saltLockStatus ? LockOpen : LockClosed" /></n-icon>
                        </template>
                      </n-button>
                    </div>
                  </n-descriptions-item>
                  <n-descriptions-item label="本地服务">
                    <div class="local-service-wrapper">
                      <n-switch v-model:value="store.state.usingLocalhostWs" :disabled="!saltLockStatus" />
                      <span class="description-text">使用本地 WebSocket 服务，更安全，最放心</span>
                    </div>
                  </n-descriptions-item>
                </n-descriptions>
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
const userInfoForm = ref(null);  // 添加表单引用

// 定义表单初始值
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
        await userInfoForm.value?.validate();
        const updatedFields = {};
        for (const key in userInfo.value) {
            if (userInfo.value[key] !== tempUserInfo.value[key]) {
                updatedFields[key] = userInfo.value[key];
            }
        }
        
        if (Object.keys(updatedFields).length === 0) {
            message.info('没有需要更新的内容');
            isEditing.value = false;
            return;
        }

        const res = await updateUserInfo(updatedFields);
        if (res.status === '200') {
            message.success('更新成功');
            isEditing.value = false;
        } else {
            message.error(res.message || '更新失败');
        }
    } catch (error) {
        console.error(error);
        message.error('表单验证失败或更新时出错');
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

const isEditing = ref(false);
const tempUserInfo = ref(null);

// 开始编辑
const startEditing = () => {
  tempUserInfo.value = { ...userInfo.value };
  isEditing.value = true;
};

// 取消编辑
const cancelEditing = () => {
  userInfo.value = { ...tempUserInfo.value };
  isEditing.value = false;
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

.profile-container {
  padding: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;

  .avatar-section {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px; // 调整间距

    .main-avatar {
      border: 4px solid #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  }

  .basic-info {
    h2 {
      margin: 0;
      font-size: 24px;
      color: #333;
    }

    .user-id {
      margin: 4px 0 0;
      color: #666;
      font-size: 14px;
    }
  }
}

.info-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h3 {
      margin: 0;
      font-size: 18px;
      color: #333;
    }
  }
}

.salt-input-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.local-service-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;

  .description-text {
    color: #666;
    font-size: 14px;
  }
}
</style>
