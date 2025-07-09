<template>
    <n-layout style="height: 100vh;">
        <n-space vertical>
            <n-layout has-sider>
                <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" :width="240" :collapsed="collapsed"
                    show-trigger @collapse="collapsed = true" @expand="collapsed = false">
                    <n-menu v-model:value="activeKey" :collapsed="collapsed" :collapsed-width="64"
                        :collapsed-icon-size="18" :options="menuOptions" />
                </n-layout-sider>
                <n-layout class="h-screen m-4">
                    <router-view />
                </n-layout>
            </n-layout>
        </n-space>
    </n-layout>

</template>
<script setup>
import { ref, h,computed } from 'vue';
import { User } from '@vicons/fa';
import { QuestionCircle32Regular, Settings48Regular } from '@vicons/fluent';
import { IosLink } from "@vicons/ionicons4";
import { CardMembershipOutlined } from '@vicons/material';
import { UserOutlined } from '@vicons/antd'
import { Terminal2 } from '@vicons/tabler';
import { Credentials, BareMetalServer } from '@vicons/carbon';
import { Terminal } from '@vicons/ionicons5';
import { NLayout, NLayoutSider, NMenu, NIcon, NSpace } from 'naive-ui';
import { RouterLink,useRouter } from 'vue-router'



const router = useRouter();
function renderIcon(icon) {
    return () => h(NIcon, null, { default: () => h(icon) })
}

const collapsed = ref(false);

// 根据router对象中的路由组 计算当前的activeKey
const currentRouteGroup = router.currentRoute.value.matched.find(route => route.name === 'account').children || [];
const activeKey = computed(() => {
    return currentRouteGroup.find(route => route.path === router.currentRoute.value.path)?.name || 'overview';
});
const menuOptions = [
    {
        label: () =>
            h(
                RouterLink,
                {
                    to: {
                        name: 'overview',
                    }
                },
                { default: () => '个人中心' }
            ),
        key: 'overview',
        icon: renderIcon(UserOutlined),
    },
    {
        label: () =>
            h(
                RouterLink,
                {
                    to: {
                        name: 'member',
                    }
                },
                { default: () => '会员中心' }
            ),
        key: 'member',
        icon: renderIcon(CardMembershipOutlined),
    },
    {
        label: () =>
            h(
                RouterLink,
                {
                    to: {
                        name: 'connectionManage',
                    }
                },
                { default: () => '连接管理' }
            ),
        key: 'connectionManage',
        icon: renderIcon(BareMetalServer),
    },
    {
        label: () =>
            h(
                RouterLink,
                {
                    to: {
                        name: 'credentials',
                    }
                },
                { default: () => '凭证中心' }
            ),
        key: 'credentials',
        icon: renderIcon(Credentials),
    },
    {
        label: '设置',
        key: '5',
        icon: renderIcon(Settings48Regular),
        children: [
            {
                label: () =>
                    h(
                        RouterLink,
                        {
                            to: {
                                name: 'termSettings',
                            }
                        },
                        { default: () => '终端设置' }
                    ),
                key: 'termSettings',
                icon: renderIcon(Terminal2),
            },
            {
                label: '系统设置',
                key: '5-2',
                icon: renderIcon(Settings48Regular),
            },
        ],
    },
    {
        label: '帮助',
        key: '6',
        icon: renderIcon(QuestionCircle32Regular),
    },
];
</script>
<style lang="scss"></style>