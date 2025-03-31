<template>
  <!-- <n-watermark v-if="watermark_show" :content="userInfo.username + userInfo.phone || ''" cross fullscreen :font-size="20"
    :line-height="20" :width="384" :height="384" :x-offset="12" :y-offset="60" :rotate="-15" /> -->
  <n-layout style="height: 100vh; position: relative;">
    <n-layout-header class="header" bordered>
      <div style="display: flex; height: 100%; align-items: center">
        <img src="@/assets/shell.svg" alt="Terminal Air" style="height: 24px;" />
        <span @click="router.push('/')" style="cursor: pointer;">
          Terminal Air
        </span>
        <div style="flex: 1;"></div>
        <div style="height: 100%; display: flex; align-items: center; margin-right: 16px;" v-if="InLogin">
          <n-icon size="24" style="cursor: pointer; margin-right: 8px;" @click="openTerminalSettings">
            <Terminal />
          </n-icon>

        </div>
        <div style="height: 100%; display: flex; align-items: center">
          <n-button v-if="!InLogin" style="margin-right: 24px;" @click="openLoginModal">
            登录
          </n-button>
          <n-popover v-else trigger="hover">
            <template #trigger>
              <div class="username_avatar">
                <n-avatar id="user-avatar" round size="large" :src="userInfo.avatar || ''" />
                <span style="margin-right: 24px; margin-left: 10px !important;">
                  {{ userInfo.nickname || userInfo.username }}
                </span>
              </div>
            </template>
            <template #header>
              <n-text @click="gotoProfileView()" depth="1">
                个人中心
              </n-text>
            </template>
            <template #footer>
              <span @click="logout">退出登录</span>
            </template>
          </n-popover>
        </div>
      </div>
    </n-layout-header>

    <!-- 主区域 -->
    <n-layout class="main-content">
      <!-- 侧边 -->
      <n-layout has-sider style="height: 100%;">
        <n-layout-sider bordered content-style="padding: 14px;" collapse-mode="width" :collapsed-width="0" :width="240"
          show-trigger="bar" show-collapsed-content="false">
          <!-- 判断是否未登录 -->
          <div v-if="!InLogin"
            style="display: flex; flex-direction: column; justify-content: center; align-items: center; height: 100%; text-align: center;">
            <p>请登录以查看连接信息。</p>
            <n-button @click="openLoginModal" type="primary">登录</n-button>
          </div>

          <!-- 已登录状态 -->
          <div v-else style="width: 100%; text-align: center;">
            <ConnectionNewItemButton @new-connection="handleNewConnection" />

            <!-- 判断是否有连接 -->
            <div
              style="display: flex; flex-direction: column; justify-content: center; align-items: center; height: 100%; text-align: center;"
              v-if="connect_list.length === 0">
              <p>请添加连接</p>
            </div>

            <!-- 有连接时显示连接列表 -->
            <div v-else>
              <div v-for="(item, index) in connect_list" :key="item">
                <ConnectionItem @taggle_connect="handleTaggleConnectionEvent"
                  @refresh_connection_list="fetchConnectionList()" :connectInfoValue="item" />
              </div>
            </div>
          </div>
        </n-layout-sider>

        <!-- 主内容 -->
        <n-layout-content
          content-style="padding: 0px; padding-left: 24px; padding-right: 12px; padding-top: 12px; padding-bottom: 12px; height: 100%; overflow: hidden;">
          <!-- 判断是否未登录 -->
          <div v-if="!InLogin"
            style="background: #000000; height: 100%; color: white; display: flex; flex-direction: column; justify-content: center; align-items: center; position: relative;">
            <img
              src="https://jrenc.azurewebsites.net/api/signature?code=zHZRCCItO-yB8t7d2KyitELFDwADnXIotkeeIQL3juyNAzFucnyrWA%3D%3D&name=Terminal%20Air&animate=true&speed=1&color=%23ffffff"
              alt="Terminal Air" style="margin-bottom: 2rem; max-width: 80%; height: auto;" />
          </div>
          <!-- 已登录状态，显示正常内容 -->
          <div style="height: 100%;" v-else>
            <SshDisplay ref="sshDisplay" />
          </div>
        </n-layout-content>
      </n-layout>
    </n-layout>

    <!-- 底部 -->
    <n-layout-footer class="footer" bordered>
      <div>
        <span>© 2024 - 2025 Terminal Air 慕垂科技 - 浙ICP备2023031974号</span>
      </div>
    </n-layout-footer>

    <!-- 新增连接面板 -->
    <AddNewConnectionDrawer @refresh_connection_list="fetchConnectionList()" />
    <EditConnectionDrawer @refresh_connection_list="fetchConnectionList()" />

    <!-- 登录/注册模态框 -->
    <n-modal v-model:show="showLoginOrRegisterModal" :mask-closable="false">
      <LoginAndRegisterModal @close="showLoginOrRegisterModal = false" />
    </n-modal>
  </n-layout>
</template>

<script setup>
import { ref, reactive,onMounted,watch, nextTick } from 'vue';
import { useStore } from 'vuex';
import { useMessage, useNotification } from 'naive-ui';
import { useRouter } from 'vue-router';

import { getUserInfo } from '@/api/auth';
import { list } from '@/api/connection';

import ConnectionNewItemButton from '@/components/ConnectionNewItemButton.vue';
import ConnectionItem from '@/components/ConnectionItem.vue';
import AddNewConnectionDrawer from '@/components/drawer/AddNewConnectionDrawer.vue';
import EditConnectionDrawer from '@/components/drawer/EditConnectionDrawer.vue';
import SshDisplay from '@/components/SshDisplay.vue';
import LoginAndRegisterModal from '@/components/modal/LoginAndRegisterModal.vue';

const store = useStore();
const message = useMessage();
const notification = useNotification();

const router = useRouter(); 
import { Terminal } from '@vicons/ionicons5';

watch(() => store.getters.isLoggedIn, (value) => {
  InLogin.value = value;
});

const showLoginOrRegisterModal = ref(false);
const connect_list = ref([]);
const userInfo = ref({});
// 绑定来自 store 的登录状态
const InLogin = ref(store.getters.isLoggedIn);

const current_connect = ref({});

const hasShownError = ref(store.getters.hasShownError);

async function fetchConnectionList() {
  try {
    const res = await list();
    if (res.status === '200') {
      connect_list.value = [...res.data];
      await nextTick();
      if (res.data.length > 0) {
        store.state.host = res.data[0].connectHost;
        store.state.port = res.data[0].connectPort;
        store.state.username = res.data[0].connectUsername;
        store.state.password = res.data[0].connectPwd;
      }
    } else {
      if (!hasShownError.value) {
        message.error(res.message || '获取连接列表失败');
        store.commit('setHasShownError', true);
        hasShownError.value = true;
      }
    }
  } catch (error) {
    if (!hasShownError.value) {
      message.error('请求连接列表时出错');
      store.commit('setHasShownError', true);
      hasShownError.value = true;
    }
  }
}

async function fetchUserInfo() {
  getUserInfo().then(res => {
    if (res.status === '200') {
      userInfo.value = res.data;
      // 检查用户昵称和邮箱是否为空
      if (!userInfo.value.nickname || !userInfo.value.email) {
        notification.warning({
          title: '提醒',
          content: '您的昵称或邮箱为空，请及时更新信息。',
          duration: 5000
        });
      }
      if (userInfo.value.isTwoFactorAuth == '0') {
        notification.warning({
          title: '提醒',
          content: '建议您开启两步验证，以提高账户安全。',
          duration: 5000
        });
      }
    } else {
      if (!hasShownError.value) {
        message.error(res.message || '获取用户信息失败');
        store.commit('setHasShownError', true);
        hasShownError.value = true;
      }
    }
  }).catch(error => {
    if (!hasShownError.value) {
      message.error('请求用户信息时出错');
      store.commit('setHasShownError', true);
      hasShownError.value = true;
    }
  });
}

const openLoginModal = () => {
  showLoginOrRegisterModal.value = true;
};

const logout = async () => {
  localStorage.removeItem('token');
  localStorage.removeItem("twoFactorAuthToken")
  InLogin.value = false;
  message.success('退出登录');
  await nextTick();
  store.dispatch('logout');
  router.push('/');
};

const gotoProfileView = () => {
  router.push('/profile');
};

const handleNewConnection = () => {
  store.state.showAddNewConnectionDrawer = true;
};

const handleTaggleConnectionEvent = (connectInfo) => {
  current_connect.value = connectInfo;
  store.state.host = connectInfo.value.connectHost;
  store.state.port = connectInfo.value.connectPort;
  store.state.username = connectInfo.value.connectUsername;
  store.state.password = connectInfo.value.connectPwd;
};

const openTerminalSettings = () => {
  store.commit('setShowTerminalSettings', true);
};

onMounted(() => {
  fetchConnectionList();
  fetchUserInfo();
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
    // margin-left: 24px;
    align-content: center;
    font-family: ui-sans-serif, -apple-system, system-ui;
  }

  img {
    margin-left: 24px;
    margin-right: 10px;
  }
}

.n-button {
  box-shadow: none !important;
}

.n-button:hover {
  box-shadow: none !important;
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

.main-content {
  margin-top: 60px;
  margin-bottom: 40px;
  height: calc(100vh - 100px);
  overflow: hidden;
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

#typed-output {
  color: white;
  margin-left: 24px;
  align-content: center;
  font-family: ui-sans-serif, -apple-system, system-ui !important;
  font-size: 24px;
  text-align: center;
  font-weight: bold;
  margin-top: 1rem;
  /* 添加上边距 */
}
</style>
