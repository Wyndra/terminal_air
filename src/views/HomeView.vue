<template>
  <n-watermark v-if="watermark_show" :content="userInfo.username || ''" cross fullscreen :font-size="20"
    :line-height="20" :width="384" :height="384" :x-offset="12" :y-offset="60" :rotate="-15" />
  <n-layout style="height: 100vh; position: relative;">
    <n-layout-header class="header" bordered>
      <div style="display: flex; height: 100%;align-items: center">
        <span>Terminal Air</span>
        <div style="flex: 1;"></div>
        <div style="height: 100%; display: flex; align-items: center">
          <n-button v-if="!InLogin" style="margin-right: 24px;" @click="openLoginModal">
            登录
          </n-button>
          <n-popover v-else trigger="hover">
            <template #trigger>
              <div class="username_avatar">
                <n-avatar id="user-avatar" round size="large"
                  src="https://s2.loli.net/2024/08/07/1wVfdgByjev7IP6.jpg" />
                <span style="margin-right: 24px;margin-left: 10px !important;">{{ userInfo.username }}</span>
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
        <!-- <n-layout-sider bordered content-style="padding: 14px;" collapse-mode="width" :collapsed-width="0" :width="240"
          show-trigger="bar">
          <ConnectionNewItemButton @new-connection="handleNewConnection" />
          <div v-for="(item, index) in connect_list" :key="index">
            <ConnectionItem @taggle_connect="handleTaggleConnectionEvent"
              @refresh_connection_list="fetchConnectionList()" :connectInfoValue="item" />
          </div>
        </n-layout-sider> -->
        <n-layout-sider bordered content-style="padding: 14px;" collapse-mode="width" :collapsed-width="0" :width="240"
          show-trigger="bar">
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
              <div v-for="(item, index) in connect_list" :key="index">
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
            style="background: #1d1f20; height: 100%; color: white; display: flex; justify-content: center; align-items: center; position: relative;">

            <!-- 打字机效果的文字 -->
            <p id="typed-output"
              style="font-size: 24px; text-align: center; font-family: 'Courier New', Courier, monospace; position: relative;">
            </p>
          </div>

          <!-- 已登录状态，显示正常内容 -->
          <div style="height: 100%;" v-else>
            <SshDisplay />
          </div>

        </n-layout-content>
      </n-layout>
    </n-layout>

    <!-- 底部 -->
    <n-layout-footer class="footer" bordered>
      <div>
        <span>© 2024 Terminal Air - 浙ICP备2023031974号</span>
      </div>
    </n-layout-footer>

    <!-- 新增连接面板 -->
    <AddNewConnectionDrawer @refresh_connection_list="fetchConnectionList()" />
    <EditConnectionDrawer @refresh_connection_list="fetchConnectionList()" />

    <!-- 登录/注册模态框 -->
    <n-modal v-model:show="showLoginOrRegisterModal">
      <LoginAndRegisterModal @close="showLoginOrRegisterModal = false" />
    </n-modal>
  </n-layout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useMessage } from 'naive-ui';
import { useRouter } from 'vue-router';

import { getUserInfo } from '@/api/auth';
import { list } from '@/api/connect';

import ConnectionNewItemButton from '@/components/ConnectionNewItemButton.vue';
import ConnectionItem from '@/components/ConnectionItem.vue';
import AddNewConnectionDrawer from '@/components/AddNewConnectionDrawer.vue';
import EditConnectionDrawer from '@/components/EditConnectionDrawer.vue';
import SshDisplay from '@/components/SshDisplay.vue';
import LoginAndRegisterModal from '@/components/LoginAndRegisterModal.vue';

const store = useStore();
const message = useMessage();

const router = useRouter();
import Typed from 'typed.js';

const startTypingEffect = () => {
  const options = {
    strings: ["请登录以查看内容"], // 需要打字的内容
    typeSpeed: 100, // 每个字符的打字速度
    backSpeed: 50, // 删除字符的速度
    backDelay: 1000, // 删除字符的延迟时间
    showCursor: false, // 显示光标
    loop: false, // 不重复打字
  };

  // 启动 typed.js 打字效果
  new Typed("#typed-output", options);
}

const showLoginOrRegisterModal = ref(false);
const connect_list = ref([]);
const userInfo = ref({});
const watermark_show = ref(false);
const InLogin = ref(!!localStorage.getItem('token'));

// 定义 current_connect
const current_connect = ref({});

// 获取 Vuex 中是否已经显示过错误
const hasShownError = ref(store.getters.hasShownError);

// 获取连接列表
async function fetchConnectionList() {
  try {
    const res = await list();
    if (res.status === '200') {
      connect_list.value = res.data;
      if (res.data.length > 0) {
        store.state.host = res.data[0].connectHost;
        store.state.port = res.data[0].connectPort;
        store.state.username = res.data[0].connectUsername;
        store.state.password = res.data[0].connectPwd;
      }
    } else {
      // 只在还未显示错误的情况下弹出错误
      if (!hasShownError.value) {
        message.error(res.message || '获取连接列表失败');
        store.commit('setHasShownError', true); // 更新 Vuex 状态，标记已显示错误
        hasShownError.value = true; // 本地更新，避免重复弹出
      }
    }
  } catch (error) {
    // 只在还未显示错误的情况下弹出错误
    if (!hasShownError.value) {
      message.error('请求连接列表时出错');
      store.commit('setHasShownError', true);
      hasShownError.value = true;
    }
  }
}

// 获取用户信息
async function fetchUserInfo() {
  getUserInfo().then(res => {
    if (res.status === '200') {
      userInfo.value = res.data;
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

// 打开登录模态框
const openLoginModal = () => {
  showLoginOrRegisterModal.value = true;
};

// 退出登录
const logout = () => {
  localStorage.removeItem('token');
  InLogin.value = false;
  location.reload();  // 刷新页面
};

const gotoProfileView = () => {
  router.push('/profile');
};

// 新建连接
const handleNewConnection = () => {
  store.state.showAddNewConnectionDrawer = true;
};

// 切换连接
const handleTaggleConnectionEvent = (connectInfo) => {
  current_connect.value = connectInfo; // 使用响应式的 current_connect
  store.state.host = connectInfo.value.connectHost;
  store.state.port = connectInfo.value.connectPort;
  store.state.username = connectInfo.value.connectUsername;
  store.state.password = connectInfo.value.connectPwd;
};

onMounted(() => {
  fetchConnectionList();
  fetchUserInfo();
  if (!InLogin.value) {
    startTypingEffect();
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
  font-size: 24px;
  text-align: center;
  font-family: 'Courier New', Courier, monospace;
}
</style>
