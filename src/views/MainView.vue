<template>
    <n-layout style="height: 100vh; position: relative;">
        <n-layout-header class="header" bordered>
            <div style="display: flex; height: 100%; align-items: center">
                <img src="@/assets/shell.svg" alt="Terminal Air" style="height: 24px;" />
                <span @click="router.push('/')" style="cursor: pointer;">
                    Terminal Air
                </span>
                <div style="flex: 1;"></div>
                <div style="height: 100%; display: flex; align-items: center; margin-right: 16px;"
                    v-if="InLogin && router.currentRoute.value.path === '/'">
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
                            <n-text @click="gotoProfileView()" depth="1" v-if="router.currentRoute.value.path === '/'">
                                个人中心
                            </n-text>
                            <n-text @click="gotoHomeView()" depth="1"
                                v-if="router.currentRoute.value.path === '/profile'">
                                回到工作区
                            </n-text>
                        </template>
                        <template #footer>
                            <div style="display: flex;justify-content: center;">
                                <span @click="logout">退出登录</span>
                            </div>
                        </template>
                    </n-popover>
                </div>
            </div>
        </n-layout-header>
        <n-layout class="main-content">
            <router-view />
        </n-layout>
        <n-layout-footer class="footer" bordered>
            <div>
                <span>© 2024 - 2025 Terminal Air 慕垂科技 - 浙ICP备2023031974号 - Commit ID: {{ gitCommitHash }}</span>
            </div>
        </n-layout-footer>
    </n-layout>
    <n-modal v-model:show="showLoginOrRegisterModal" :mask-closable="false">
        <LoginAndRegisterModal @close="showLoginOrRegisterModal = false" />
    </n-modal>
</template>
<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { useStore } from 'vuex';
import { useMessage, useNotification } from 'naive-ui';
import { useRouter } from 'vue-router';
import { getUserInfo } from '@/api/auth';
import { list } from '@/api/connection';
import LoginAndRegisterModal from '@/components/modal/LoginAndRegisterModal.vue';

const store = useStore();
const message = useMessage();
const notification = useNotification();

const router = useRouter();
import { Terminal } from '@vicons/ionicons5';

watch(() => store.getters.isLoggedIn, (value) => {
    InLogin.value = value;
});

const gitCommitHash = process.env.VUE_APP_GIT_COMMIT_HASH;

const showLoginOrRegisterModal = ref(false);
const connect_list = ref([]);
const userInfo = ref({});
const InLogin = ref(store.getters.isLoggedIn);

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
                store.state.credentialUUID = res.data[0].credentialUUID;
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

const gotoHomeView = () => {
    router.push("/")
}

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