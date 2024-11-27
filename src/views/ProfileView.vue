<template>
    <n-watermark v-if="watermark_show" :content="userInfo.username || ''" cross fullscreen :font-size="20"
        :line-height="20" :width="384" :height="384" :x-offset="12" :y-offset="60" :rotate="-15" />
    <n-layout style="height: 100vh; position: relative;">
        <n-layout-header class="header" bordered>
            <div style="display: flex; height: 100%;">
                <span>Terminal Air</span>
                <div style="flex: 1;"></div>
                <div style="height: 100%; display: flex; align-items: center">
                    <n-button v-if="!InLogin" style="margin-right: 24px;" @click="openLoginModal">
                        登录
                    </n-button>
                    <n-popover v-else trigger="hover">
                        <template #trigger>
                            <n-avatar style="margin-right: 24px;" round size="large"
                                src="https://s2.loli.net/2024/08/07/1wVfdgByjev7IP6.jpg" />
                        </template>
                        <template #header>
                            <n-text @click="gotoUserInfo()" depth="1">
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
                <!-- 主内容 -->
                <n-layout-content
                    content-style="padding: 0px; padding-left: 24px; padding-right: 12px; padding-top: 12px;padding-bottom: 12px;height: 100%; overflow: hidden;">
                    <n-card title="基本信息">
                        <n-tabs type="line" animated>
                            <n-tab-pane name="个人信息">
                                <!-- 个人信息表单 -->
                                 <n-form
                                    :model="userInfo"
                                    label-placement="left"
                                    label-width="100px"
                                    :rules="rules"
                                    ref="userInfoForm"
                                >
                                    <n-form-item label="用户名" required>
                                        <n-input v-model="userInfo.username" />
                                    </n-form-item>
                                    <n-form-item label="邮箱" required>
                                        <n-input v-model="userInfo.email" />
                                    </n-form-item>
                                    <n-form-item label="手机号" required>
                                        <n-input v-model="userInfo.phone" />
                                    </n-form-item>
                                    <n-form-item>
                                        <n-button type="primary" @click="submitUserInfoForm">保存</n-button>
                                    </n-form-item>
                                </n-form>
                            </n-tab-pane>
                            <n-tab-pane name="安全设置">
                                <n-space direction="vertical">
                                    <n-text>密码：********</n-text>
                                    <n-text>密保问题：********</n-text>
                                </n-space>
                            </n-tab-pane>
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

        <!-- 底部 -->
        <n-layout-footer class="footer" bordered>
            <div>
                <span>© 2024 Terminal Air</span>
            </div>
        </n-layout-footer>

        <!-- 连接面板 -->
        <AddNewConnectionDrawer />

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
import SshDisplay from '@/components/SshDisplay.vue';
import LoginAndRegisterModal from '@/components/LoginAndRegisterModal.vue';

const store = useStore();
const message = useMessage();

const router = useRouter();

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
    try {
        const res = await getUserInfo();
        if (res.status === '200') {
            userInfo.value = res.data;
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
    }
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

const gotoUserInfo = () => {
    console.log('gotoUserInfo');
    router.push('/userinfo');
};

// 新建连接
const handleNewConnection = () => {
    store.state.showAddNewConnectionDrawer = true;
};

// 切换连接
const handleTaggleConnection = (connectInfo) => {
    current_connect.value = connectInfo; // 使用响应式的 current_connect
    store.state.host = connectInfo.value.connectHost;
    store.state.port = connectInfo.value.connectPort;
    store.state.username = connectInfo.value.connectUsername;
    store.state.password = connectInfo.value.connectPwd;
};

onMounted(() => {
    fetchConnectionList();
    fetchUserInfo();
});
</script>
<style scoped lang="less">
.n-card {
    margin-bottom: 12px;
    max-width: 800px;
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
</style>
