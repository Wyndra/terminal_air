<template>
  <!-- Main Content -->
  <n-layout style="height: 100%;">
    <n-layout-content content-style="padding: 24px;display: flex;gap:20px">
      <n-card :title="cardName" style="max-width: 800px;">
        <n-tabs type="line" animated @before-leave="handleBeforeLeave">
          <!-- 个人信息标签页 -->
          <n-tab-pane name="个人信息" tab="个人信息">
            <div class="profile-container">
              <!-- 头像和基本信息区域 -->
              <div class="profile-header">
                <div class="avatar-section"
                  style="display: flex;justify-content: center !important;align-items: center;">
                  <n-avatar round :size="120" :src="userInfo.avatar || ''" class="main-avatar"></n-avatar>
                  <n-upload :action="uploadUrl" :max-size="2097152" accept="image/*" @before-upload="handleBeforeUpload"
                    @error="handleUploadError" :custom-request="customUpload">
                    <n-button type="text" size="small" class="change-avatar-btn">创建你的头像</n-button>
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
                </div>
                <!-- 展示模式 -->
                <n-descriptions :column="2" label-align="left" style="margin-top: 16px">
                  <n-descriptions-item v-for="(field, key) in editableFields" :key="field.key" :label="field.label"
                    class="info-item">
                    <div class="value-wrapper">
                      <n-text>{{ userInfo[field.key] }}</n-text>
                      <n-button v-if="field.editable" secondary size="small" text @click="openEditDialog(field)">
                        <n-text style="color: cornflowerblue;">
                          编辑
                        </n-text>
                      </n-button>
                    </div>
                  </n-descriptions-item>
                </n-descriptions>

                <!-- 编辑弹窗 -->
                <n-modal v-model:show="showEditModal" preset="card" :title="`修改${currentField?.label || ''}`"
                  style="width:500px" :bordered="false" transform-origin="center">

                  <n-form :model="editForm" ref="editFormRef" :rules="getFieldRules" label-placement="left">
                    <n-form-item :path="currentField?.key || ''" :label="currentField?.label">
                      <n-input v-model:value="editForm.value" :placeholder="`请输入${currentField?.label || ''}`" />
                    </n-form-item>
                  </n-form>
                  <!-- <template #action> -->
                  <n-space style="justify-content: flex-end;">
                    <n-button size="small" @click="showEditModal = false">取消</n-button>
                    <n-button size="small" type="primary" @click="handleFieldUpdate">确认</n-button>
                  </n-space>
                  <!-- </template> -->
                </n-modal>
              </div>
            </div>
          </n-tab-pane>
          <n-tab-pane name="帐号安全" tab="帐号安全">
            <n-form label-placement="left" label-width="120px" label-align="left">
              <n-form-item label="登录密码">
                <div class="two-factor-wrapper">
                  <n-button @click="showChangePasswordModal = true">更改密码</n-button>
                  <span class="description-text">当发现自己密码泄露，应立即更改密码。</span>
                </div>
              </n-form-item>
            </n-form>
            <n-form :model="userInfo" label-placement="left" label-width="120px" label-align="left">
              <n-form-item label="双重认证">
                <div class="two-factor-wrapper">
                  <n-button @click="handleTwoFactorAuthClick">{{ twoFactorAuthStatus ?
                    "更改验证方法" : "添加验证方法" }}</n-button>
                  <span class="description-text">使用一次性代码验证你的身份，以确保你的帐号安全。</span>
                </div>
              </n-form-item>
            </n-form>
          </n-tab-pane>
        </n-tabs>
      </n-card>
      <CredentialsManage />
    </n-layout-content>
  </n-layout>


  <!-- 密钥 密码解锁弹出框 -->
  <UseLockByPasswordModal v-model:show="showLockByPasswordModal"
    @unlockByPasswordEvent="handleVerifyUserPasswordResult" />

  <!-- 双重认证密码解锁弹出框 -->
  <UseLockByPasswordModal v-model:show="showTwoFactorAuthLockByPasswordModal"
    @unlockByPasswordEvent="handleTwoFactorAuthVerifyResult" />

  <!-- 双重认证管理弹出框 -->
  <TwoFactorAuthManageModal v-model:show="showTwoFactorAuthManageModal" @close="showTwoFactorAuthManageModal = false"
    @twoFactorAuthResultEvent="handleTwoFactorAuthResult" />

  <TwoFactorAuthVerifyModal @verifySuccess="handleTwoFactorAuthSuccess" v-model:show="showSelectVerifyMethodModal"
    @close="showSelectVerifyMethodModal = false" />

  <!-- 密码修改弹出框 -->
  <ChangePasswordModal v-model:show="showChangePasswordModal" @close="showChangePasswordModal = false" />

  <!-- TOTP 解锁弹出框 -->
  <UseLockByTotpModal v-model:show="showLockByTotpModal" @unlockByTotpEvent="handleVerifyUserTotpResult" />

  <!-- 登录/注册模态框 -->
  <n-modal v-model:show="showLoginOrRegisterModal">
    <LoginAndRegisterModal @close="showLoginOrRegisterModal = false" />
  </n-modal>

  <!-- 验证码弹窗 -->
  <n-modal v-model:show="showVerifyModal" preset="card" :title="`验证${currentField?.label || ''}`" style="width: 500px">
    <n-form>
      <n-form-item label="验证码">
        <n-input v-model:value="phone" placeholder="请输入手机号" />
        <n-input-group>
          <n-input v-model:value="verifyCode" placeholder="请输入验证码" />
          <n-button type="primary" ghost>获取验证码</n-button>
        </n-input-group>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button size="small" @click="showVerifyModal = false">取消</n-button>
        <n-button size="small" type="primary" :disabled="!verifyCode" @click="handleVerifySubmit">
          确认
        </n-button>
      </n-space>
    </template>
  </n-modal>

  <!-- 新手机号验证弹窗 -->
  <n-modal v-model:show="showVerifyModal" preset="card" :title="`验证${currentField?.label || ''}`" style="width: 500px">
    <n-form :model="phoneForm" :rules="phoneRules" ref="phoneFormRef">
      <n-form-item label="新手机号" path="newPhone">
        <n-input v-model:value="phoneForm.newPhone" placeholder="请输入新手机号" />
      </n-form-item>
      <n-form-item label="验证码" path="verifyCode">
        <n-input-group>
          <n-input v-model:value="phoneForm.verifyCode" placeholder="请输入验证码" />
          <n-button :disabled="!canSendCode" type="primary" ghost @click="handleSendCode">
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </n-button>
        </n-input-group>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button size="small" @click="showPhoneVerifyModal = false">取消</n-button>
        <n-button size="small" type="primary" :disabled="!phoneForm.verifyCode" @click="handlePhoneUpdate">
          确认
        </n-button>
      </n-space>
    </template>
  </n-modal>
  <!-- 验证原手机号弹窗 -->
  <n-modal v-model:show="showVerifyOldPhoneModal" preset="card" title="验证原手机号" style="width: 500px">
    <n-form :model="oldPhoneForm" :rules="oldPhoneRules" ref="oldPhoneFormRef">
      <n-form-item label="原手机号" path="oldPhone">
        <n-input v-model:value="oldPhoneForm.oldPhone" placeholder="请输入原手机号" disabled />
      </n-form-item>
      <n-form-item label="验证码" path="verifyCode">
        <n-input-group>
          <n-input v-model:value="oldPhoneForm.verifyCode" placeholder="请输入验证码" />
          <n-button :disabled="oldPhoneForm.oldPhone == undefined" type="primary" ghost @click="handleSendOldPhoneCode">
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </n-button>
        </n-input-group>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button size="small" @click="showVerifyOldPhoneModal = false">取消</n-button>
        <n-button size="small" type="primary" :disabled="!oldPhoneForm.verifyCode" @click="handleVerifyOldPhoneSubmit">
          确认
        </n-button>
      </n-space>
    </template>
  </n-modal>


</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useStore } from 'vuex';
import { useMessage } from 'naive-ui';
import { getUserInfo, updateUserInfo, getUserAvatar } from '@/api/auth';
import { switchTwoFactorAuth, getTwoFactorAuthSecretQRCode, getTwoFactorAuthStatus } from '@/api/mfa';
import { sendSmsCodeByToken, sendVerificationCode, verifyCode } from '@/api/sms';
import { generatePresignUrl } from "@/api/avatar"
import axios from 'axios';
import router from '@/router';
import { userInfoRules } from '@/constant/rules';
import LoginAndRegisterModal from '@/components/modal/LoginAndRegisterModal.vue';
import ChangePasswordModal from '@/components/modal/ChangePasswordModal.vue';
import UseLockByPasswordModal from '@/components/modal/UseLockByPasswordModal.vue';
import UseLockByTotpModal from '@/components/modal/UseLockByTOTPModal.vue';
import TwoFactorAuthManageModal from '@/components/modal/TwoFactorAuthManageModal.vue';
import CredentialsManage from '@/components/card/CredentialsManageCard.vue';
import TwoFactorAuthVerifyModal from '@/components/modal/TwoFactorAuthVerifyModal.vue';


const store = useStore();
const message = useMessage();
const userInfoForm = ref(null);  // 添加表单引用
const cardName = ref("个人中心");
const showLoginOrRegisterModal = ref(false);
const showSelectVerifyMethodModal = ref(false);

// 定义表单初始值
const userInfo = ref({
  uid: 0,
  username: '',
  password: '',
  email: '',
  salt: '',
  nickname: '',
  phone: '',
  avatar: '',
});

const twoFactorAuthStatus = ref(false);

const handleTwoFactorAuthClick = async () => {
  if (twoFactorAuthStatus.value) {
    // 如果已经启用双重认证，选择验证方式
    showSelectVerifyMethodModal.value = true;
  } else {
    // 如果未启用双重认证，则显示密码验证弹窗
    showLockByPasswordModal.value = true;
  }
}

const handleTwoFactorAuthSuccess = async () => {
  // 处理双重认证成功后的逻辑
  showSelectVerifyMethodModal.value = false;
  showTwoFactorAuthManageModal.value = true;
};

const safeSettingForm = ref({
  salt: ''
});
const saltLockStatus = ref(false);
const showLockByPasswordModal = ref(false);
const showLockByTotpModal = ref(false);

// 双重验证相关
const showTwoFactorAuthLockByPasswordModal = ref(false);
const showTwoFactorAuthManageModal = ref(false);

// 密码修改相关
const showChangePasswordModal = ref(false);

const handleTwoFactorAuthVerifyResult = (isUnlocked) => {
  if (isUnlocked) {
    // 解锁成功，并关闭弹窗
    // showTwoFactorAuthLockByPasswordModal.value = false;
    showTwoFactorAuthManageModal.value = true
  } else {
    saltLockStatus.value = false;
  }
};

const isShaking = ref(false);
const InLogin = ref(store.getters.isLoggedIn);
const hasShownError = ref(store.getters.hasShownError);
const isLoading = ref(true);  // 添加一个加载状态

const handleBeforeLeave = (tabName) => {
  if (tabName === '安全设置') {
    saltLockStatus.value = false;
  }
  cardName.value = tabName;
  return true;
};

async function fetchUserInfo() {
  try {
    const res = await getUserInfo();
    if (res.status === '200') {
      const userData = res.data;
      userInfo.value = userData;
      localStorage.removeItem('userAvatar');
      localStorage.setItem('userAvatar', userInfo.value.avatar);
      if (userData.twoFactorAuth) {
        await fetchQRCode();
      }
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
    showLockByPasswordModal.value = false;
    showTwoFactorAuthManageModal.value = true
  } else {
  }
};

const handleVerifyUserTotpResult = (isUnlocked) => {
  if (isUnlocked) {
    // 解锁成功，并关闭弹窗
    saltLockStatus.value = true;
    showLockByTotpModal.value = false;
  } else {
    saltLockStatus.value = false;  // 解锁失败，继续显示弹窗
  }
};

const handleTwoFactorAuthResult = (isVerify) => {
  if (isVerify) {
    // 解锁成功，并关闭弹窗
    showTwoFactorAuthManageModal.value = false;
    handleTwoFactorChange();
  } else {
    saltLockStatus.value = false;  // 解锁失败，继续显示弹窗
  }
};

const handleLockByPassword = () => {
  if (saltLockStatus.value) {
    saltLockStatus.value = false;
  } else {
    if (userInfo.value.twoFactorAuth) {
      showLockByTotpModal.value = true;
    } else {
      showLockByPasswordModal.value = true;
    }
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
  localStorage.removeItem("twoFactorAuthToken")
  InLogin.value = false;
  store.dispatch('logout');
  router.push('/');
};

const gotoHomeView = () => {
  router.push('/')
};

// 上传头像
const customUpload = async ({ file, onProgress, onError }) => {
  if (!file || !file.file) { // 添加文件存在性检查
    message.error('未选择文件');
    onError(new Error('未选择文件'));
    return;
  }
  let uploadUrl = '';
  let filePath = ""
  // 先向服务器请求一个预签名的上传地址
  await generatePresignUrl().then(res => {
    uploadUrl = res.data.url;
    filePath = res.data.filePath
  }).catch(err => {
    console.error(err)
  });

  console.log(file.file)


  try {
    const response = await axios.put(uploadUrl, file.file, {
      headers: {
        'Content-Type': file.file.type  // 确保 Content-Type 正确
      },
      onUploadProgress: (event) => {
        if (event.lengthComputable) {
          const percent = Math.round((event.loaded * 100) / event.total);
          onProgress({ percent });
        }
      }
    }).then(async res => {
      if (res.status == 200) {
        await updateUserInfo({
          uid: userInfo.value.uid,
          avatar: filePath
        }).then(async res => {
          if (res.status == 200) {
            message.success('头像更新成功');
            await getUserAvatar().then(res => {
              userInfo.value.avatar = res.data
              localStorage.setItem('userAvatar', res.data)
            })
          } else {
            message.error('头像更新失败');
          }
        })
      }
    })
    // 开始处理服务器数据。
  } catch (error) {
    console.error("Upload failed:", error);
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

// 上传错误处理
const handleUploadError = () => {
  message.error('头像上传失败');
};

const isEditing = ref(false);
const tempUserInfo = ref(null);

const editableFields = [
  { key: 'username', label: '用户名', editable: false },
  { key: 'nickname', label: '昵称', editable: true },
  {
    key: 'phone',
    label: '手机号码',
    editable: true,
    needVerify: true, // 需要验证码验证
    verifyType: 'sms' // 验证方式
  },
  { key: 'email', label: '邮箱地址', editable: true },
  { key: 'createTime', label: "注册时间", editable: false }
];

// 添加验证码相关的响应式变量
const showVerifyModal = ref(false);
const verifyCodeInput = ref(''); // 修改变量名避免冲突
const updatedValue = ref(''); // 暂存要更新的值

// 修改openEditDialog方法
const openEditDialog = (field) => {
  if (!field.editable) return;
  currentField.value = field;

  if (field.key === 'phone') {
    oldPhoneForm.value.oldPhone = userInfo.value.phone;
    oldPhoneForm.value.verifyCode = '';
    showVerifyOldPhoneModal.value = true; // 显示原手机号校验弹窗
  } else {
    editForm.value.value = userInfo.value[field.key];
    showEditModal.value = true;
  }
};

// 添加验证码处理方法
const handleVerifySubmit = async () => {
  try {
    const verified = await verifySmsCode({ code: verifyCodeInput.value });
    if (verified.data.success) {
      message.success('验证成功');
      showVerifyModal.value = false;
      showEditModal.value = true; // 显示编辑弹窗
    } else {
      message.error('验证码错误');
    }
  } catch (error) {
    message.error('验证失败');
  }
};

const showEditModal = ref(false);
const currentField = ref(null);
const editForm = ref({ value: '' });
const editFormRef = ref(null);

const getFieldRules = computed(() => {
  if (!currentField.value) return {};
  return { value: userInfoRules[currentField.value.key] };
});

const handleFieldUpdate = async () => {
  try {
    await editFormRef.value?.validate();
    const updatedField = {
      [currentField.value.key]: editForm.value.value,
      uid: userInfo.value.uid // 保持 uid
    };

    const res = await updateUserInfo(updatedField);
    if (res.status === '200') {
      message.success('更新成功');
      userInfo.value[currentField.value.key] = editForm.value.value;
      showEditModal.value = false;

      // 如果修改了昵称,需要更新显示
      if (currentField.value.key === 'nickname') {
        await fetchUserInfo();
      }
      fetchUserInfo();
    } else {
      message.error(res.message || '更新失败');
    }
  } catch (error) {
    console.error(error);
    message.error('验证失败或更新时出错');
  }
};

// 手机号相关
const showPhoneVerifyModal = ref(false);
const phoneFormRef = ref(null);
const phoneForm = ref({
  newPhone: '',
  verifyCode: ''
});
const countdown = ref(0);
const canSendCode = computed(() => {
  return /^1[3-9]\d{9}$/.test(phoneForm.value.newPhone) && countdown.value === 0;
});

const phoneRules = {
  newPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码格式不正确', trigger: 'blur' }
  ]
};

// 发送验证码
const handleSendCode = async () => {
  try {
    // await phoneFormRef.value?.validate();
    // 调用发送验证码接口
    const res = await sendVerificationCode({ phone: phoneForm.value.newPhone, channel: '1021' })
    localStorage.setItem('newPhone', phoneForm.value.newPhone);
    localStorage.setItem('newSerial', res.data.serial);

    // 开始倒计时
    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);

    message.success('验证码已发送');
  } catch (error) {
    message.error('发送验证码失败');
  }
};

// 更新手机号
const handlePhoneUpdate = async () => {
  try {
    await phoneFormRef.value?.validate();
    // 调用验证码验证接口
    const verified = await verifyCode({
      phone: localStorage.getItem('newPhone'),
      serial: localStorage.getItem('newSerial'),
      code: phoneForm.value.verifyCode
    })

    if (verified.data) {
      // 验证通过后更新手机号
      const res = await updateUserInfo({
        phone: phoneForm.value.newPhone,
        uid: userInfo.value.uid
      });

      if (res.status === '200') {
        message.success('手机号更新成功');
        showPhoneVerifyModal.value = false;
        showVerifyModal.value = false;
        await fetchUserInfo();
        localStorage.removeItem('newPhone');
        localStorage.removeItem('newSerial');
      } else {
        message.error(res.message || '更新失败');
      }
    } else {
      message.error('验证码错误');
    }
  } catch (error) {
    message.error('验证失败或更新出错');
  }
};

// 处理双重认证开关
const handleTwoFactorChange = async (value) => {
  try {
    const res = await switchTwoFactorAuth();
    if (res.status === '200') {
      userInfo.value.twoFactorAuth = res.data;
      message.success(userInfo.value.twoFactorAuth ? '双重认证已开启' : '双重认证已关闭');

      // 如果开启了双重认证，获取二维码
      if (userInfo.value.twoFactorAuth) {
        fetchQRCode();
      } else {
        qrcodeImage.value = '';
      }
    } else {
      // 如果失败，回滚开关状态为原来的状态
      userInfo.value.twoFactorAuth = value;
      message.error(res.message || '操作失败');
    }
  } catch (error) {
    // 发生错误时保持原状态
    userInfo.value.twoFactorAuth = value;
    message.error('操作失败');
  }
};

const qrcodeImage = ref("");

// 获取二维码
const fetchQRCode = async () => {
  try {
    const res = await getTwoFactorAuthSecretQRCode();
    if (res.status === '200') {
      qrcodeImage.value = res.data;
    } else {
      message.error('获取二维码失败');
    }
  } catch (error) {
    message.error('获取二维码失败');
  }
};

const showVerifyOldPhoneModal = ref(false);
const oldPhoneForm = ref({
  oldPhone: '',
  verifyCode: ''
});
const oldPhoneRules = {
  oldPhone: [
    { required: true, message: '请输入原手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码格式不正确', trigger: 'blur' }
  ]
};

const canSendOldPhoneCode = computed(() => {
  return countdown.value === 0;
});

const oldPhoneData = ref({
  phone: '',
  serial: ''
});

const handleSendOldPhoneCode = async () => {
  try {
    // 调用发送验证码接口
    const res = await sendSmsCodeByToken();
    oldPhoneData.value.phone = res.data.phone;
    oldPhoneData.value.serial = res.data.serial;
    localStorage.setItem('oldPhone', res.data.phone);
    localStorage.setItem('oldSerial', res.data.serial);

    // 开始倒计时
    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);

    message.success('验证码已发送');
  } catch (error) {
    message.error('发送验证码失败');
  }
};

const handleVerifyOldPhoneSubmit = async () => {
  try {
    const verified = await verifyCode({
      phone: oldPhoneData.value.phone,
      serial: oldPhoneData.value.serial,
      code: oldPhoneForm.value.verifyCode
    });
    if (verified.data) {
      message.success('原手机号验证成功');
      showVerifyOldPhoneModal.value = false;
      showVerifyModal.value = true; // 显示新手机号校验弹窗
      localStorage.removeItem('oldPhone');
      localStorage.removeItem('oldSerial');
    } else {
      message.error('验证码错误');
    }
  } catch (error) {
    message.error('验证失败');
  }
};

onMounted(async () => {
  if (InLogin.value) {
    fetchUserInfo();
  }
  await getTwoFactorAuthStatus().then(res => {
    if (res.status == 200) {
      twoFactorAuthStatus.value = res.data;
    }
  }).catch(err => {
    console.error(err)
  })
});
</script>

<style scoped lang="less">
span {
  font-family: ui-sans-serif, -apple-system, system-ui;
}

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
    // margin-left: 24px;
    align-content: center;
    font-family: ui-sans-serif, -apple-system, system-ui;
  }

  img {
    margin-left: 24px;
    margin-right: 10px;
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
      font-family: ui-sans-serif, -apple-system, system-ui;
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
  margin-top: 8px;
  gap: 12px;
  align-items: center;
}

.local-service-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;

  .description-text {
    color: #666;
    font-size: 12px;
  }
}

.description-item {
  display: flex;
  align-items: center;
  gap: 4px;

  span {
    flex: 1;
  }

  .n-button {
    padding: 2px;
    margin: -2px -6px -2px 0;
    color: var(--n-text-color-3);

    &:hover {
      color: var(--n-primary-color);
    }
  }
}

.info-item {
  :deep(.n-descriptions-item-content) {
    padding: 12px 0;
  }
}

.value-wrapper {
  align-items: center;
  gap: 12px;
  display: flex;
  justify-content: space-between;

  .n-text {
    flex: 1;
    color: var(--n-text-color);
    font-family: ui-sans-serif, -apple-system, system-ui;
  }

  .n-button {
    flex-shrink: 0;
    min-width: 48px;
    font-size: 12px;
    padding: 2px 8px;
    height: 24px;
    opacity: 0.8;
    transition: all 0.2s ease;

    &:hover {
      opacity: 1;
    }
  }
}

:deep(.n-input-group) {
  display: flex;
  gap: 8px;

  .n-input {
    flex: 1;
  }

  .n-button {
    min-width: 100px;
  }
}

.two-factor-wrapper {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  flex-direction: column;
  // align-items: center;

  .description-text {
    color: #a1a1a1;
    font-size: 14px;
  }
}

.qrcode-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 0;

  .qrcode-wrapper {
    background: #fff;
    padding: 8px;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .qrcode-tips {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    font-family: ui-sans-serif, -apple-system, system-ui;
  }
}

.change-avatar-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-family: ui-sans-serif, -apple-system, system-ui;
  color: cornflowerblue;
}

.change-avatar-btn:hover {
  color: cornflowerblue;
}

.change-avatar-btn:active {
  color: cornflowerblue;
}
</style>
