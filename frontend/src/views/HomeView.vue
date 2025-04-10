<template>
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
            <ConnectionItem @taggle_connect="handleTaggleConnectionEvent" @refresh="fetchConnectionList()"
              :connectionValue="item" />
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
        <img src="@/assets/svg/signature.svg" alt="Terminal Air" style="margin-bottom: 2rem; max-width: 80%; height: auto;" />
      </div>
      <!-- 已登录状态，显示正常内容 -->
      <div style="height: 100%;" v-else>
        <SshDisplay ref="sshDisplay" />
      </div>
    </n-layout-content>
  </n-layout>

  <!-- 新增连接面板 -->
  <AddNewConnectionDrawer @refresh_connection_list="fetchConnectionList()" />

  <!-- 登录/注册模态框 -->
  <n-modal v-model:show="showLoginOrRegisterModal" :mask-closable="false">
    <LoginAndRegisterModal @close="showLoginOrRegisterModal = false" />
  </n-modal>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { useStore } from 'vuex';
import { useMessage } from 'naive-ui';
import { list } from '@/api/connection';

import ConnectionNewItemButton from '@/components/ConnectionNewItemButton.vue';
import ConnectionItem from '@/components/ConnectionItem.vue';
import AddNewConnectionDrawer from '@/components/drawer/AddConnectionDrawer.vue';
import SshDisplay from '@/components/SshDisplay.vue';
import LoginAndRegisterModal from '@/components/modal/LoginAndRegisterModal.vue';

const store = useStore();
const message = useMessage();
import { Terminal } from '@vicons/ionicons5';

watch(() => store.getters.isLoggedIn, (value) => {
  InLogin.value = value;
});

const showLoginOrRegisterModal = ref(false);
const connect_list = ref([]);
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
        store.state.method = Number(res.data[0].connectMethod);
        store.state.credentialId = res.data[0].credentialUUID;
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
      message.error(error.response.data.message);
      store.commit('setHasShownError', true);
      hasShownError.value = true;
    }
  }
}

const openLoginModal = () => {
  showLoginOrRegisterModal.value = true;
};

const handleNewConnection = () => {
  store.state.showAddConnectionDrawer = true;
};

const handleTaggleConnectionEvent = (connectInfo) => {
  current_connect.value = connectInfo;
  store.state.host = connectInfo.value.connectHost;
  store.state.port = connectInfo.value.connectPort;
  store.state.username = connectInfo.value.connectUsername;
  store.state.password = connectInfo.value.connectPwd;
  store.state.method = connectInfo.value.connectMethod;
  store.state.credentialId = connectInfo.value.credentialUUID;
};

onMounted(() => {
  InLogin.value == true ? fetchConnectionList() : null;
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
