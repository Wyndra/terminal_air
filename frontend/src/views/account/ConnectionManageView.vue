<template>
    <n-card title="连接管理" :style="{ height: 'calc(100% - 200px)' }"
        content-style="display: flex; flex-direction: column;gap: 20px;">
        <template #header-extra>
            <div style="display: flex;align-items: center;">
                <n-icon :size="18">
                    <QuestionCircle32Regular />
                </n-icon>
                <span> 了解 连接 必读</span>
            </div>

        </template>
        <div class="view-header">

            <div class="main-area">
                <n-button type="primary" style="width: 120px;"
                    @click="store.state.showAddConnectionDrawer = true;">新增连接</n-button>
            </div>
            <div class="other-area">
                <n-button circle size="small"
                    @click="() => { fetchConnections(); loading(); message.success('刷新成功'); }">
                    <n-icon>
                        <RefreshSharp />
                    </n-icon>
                </n-button>
                <n-popselect v-model:value="currentCredentialsColumnsOptions" multiple
                    :options="credentialsColumnsOptions" size="medium">
                    <template #header>
                        列展示选择
                    </template>
                    <n-button circle size="small" @click="() => { fetchConnections(); loading(); }">
                        <n-icon>
                            <Settings48Regular />
                        </n-icon>
                    </n-button>
                </n-popselect>
            </div>

        </div>
        <n-spin size="large" :show="onloading">
            <!-- <n-data-table :columns="credentialsColumns" :data="credentialsData" :bordered="false"> -->
            <n-data-table :columns="filteredColumns" :data="connectionsData" :bordered="false">
                <template #empty>
                    <div style="text-align: center; font-size: 16px; color: #7f7e7a;">
                        暂无凭证
                    </div>
                </template>
            </n-data-table>
        </n-spin>


    </n-card>
    <CreateCredentialsModal v-model:show="createCredentialsModalVisible" @close="handleCreateClose"
        @refresh="fetchCredentials" :currentCredentialsList="credentialsData" />

    <BindCredentialsModal v-model:show="bindCredentialsModalVisible" @close="handleBindClose"
        @refresh="fetchCredentials" :row="currentRow" />

    <!-- 新增连接面板 -->
    <AddNewConnectionDrawer @refresh_connection_list="fetchConnectionList()" />

</template>
<script setup>
import { ref, onMounted, h, computed } from 'vue';
import { listCredentials, deleteCredentials } from '@/api/credentials';
import { list,del } from "@/api/connection";
import { useMessage, NTag, NButton, NPopconfirm, NDataTable } from "naive-ui";
import { QuestionCircle32Regular, Settings48Regular } from '@vicons/fluent';
import { RefreshSharp } from "@vicons/ionicons5"
import { useStore } from 'vuex';
import CreateCredentialsModal from '@/components/modal/CreateCredentialsModal.vue';
import BindCredentialsModal from '@/components/modal/BindCredentialsModal.vue';
import AddNewConnectionDrawer from '@/components/drawer/AddConnectionDrawer.vue';
import TableActions from '@/components/TableActions.vue';
import ShowPasswordControl from '@/components/ShowPasswordControl.vue'

const store = useStore();

const message = useMessage();
const createCredentialsModalVisible = ref(false);
const bindCredentialsModalVisible = ref(false);
const handleCreateClose = () => {
    createCredentialsModalVisible.value = false;
};
const handleBindClose = () => {
    bindCredentialsModalVisible.value = false;
};

const currentRow = ref({})
const onloading = ref(false);
const loading = () => {
    onloading.value = true;
    setTimeout(() => {
        onloading.value = false
    }, 300);
};


const connectionsColumns = [
    {
        title: '连接名称',
        key: 'connectName',
        className: 'columns',
        // align: 'center',
    },
    {
        title: '连接地址',
        key: 'connectHost',
        className: 'columns',
        align: 'center',
    },
    {
        title: '连接端口',
        key: 'connectPort',
        className: 'columns',
        align: 'center',
    },
    {
        title: '登录用户名',
        key: 'connectUsername',
        className: 'columns',
        align: 'center',
    },
    {
        title: '认证方式',
        key: 'connectMethod',
        className: 'columns',
        align: 'center',
        render(row) {
            return h(NTag, { type:'success',size: "small" }, () => row.connectMethod === "0" ? '密码认证' : '密钥认证')
        }
    },
    {
        title: '登录密码',
        key: 'connectPwd',
        className: 'columns',
        align: 'center',
        render(row) {
            return h(ShowPasswordControl,{
                text: row.connectPwd,
            })
        }
    },
    {
        title: '认证密钥',
        key: 'credentialUUID',
        className: 'columns',
        align: 'center',
        render(row) {
            return row.credentialUUID
                ? h(NTag, { type: 'success', size: "small" }, () => '已绑定密钥')
                : h(NTag, { type: 'warning', size: "small" }, () => '无绑定密钥')
        }
    },
    {
        title: '操作',
        key: 'actions',
        className: 'columns',
        align: 'center',
        render(row) {
            return h(TableActions, {
                row,
                actions: [
                    { label: '绑定', event: 'bind', type: 'primary', quaternary: true },
                    {
                        label: '删除',
                        event: 'delete',
                        type: 'error',
                        confirm: true,
                        confirmText: '确定要删除该连接吗？该操作无法恢复',
                    },
                ],
                onAction: (event, row) => handleTableAction(event, row),
            })
        },
    },
]

// 可选列配置（用于列选择器）
const credentialsColumnsOptions = [
    { label: '凭证名称', value: 'connectName' },
    { label: '主机地址', value: 'connectHost' },
    { label: '连接端口', value: 'connectPort' },
    { label: '登录用户名', value: 'connectUsername' },
    { label: '认证方式', value: 'connectMethod' },
    { label: '登录密码', value: 'connectPwd' },
    { label: '认证密钥', value: 'credentialUUID' },
    { label: '操作', value: 'actions' }
]

// 当前选中的列（默认全部）
const currentCredentialsColumnsOptions = ref(
    credentialsColumnsOptions.map((item) => item.value)
)

// 实际用于展示的列（响应式计算）
const filteredColumns = computed(() => {
    return connectionsColumns.filter((col) => {
        if (!col.key) return true
        return currentCredentialsColumnsOptions.value.includes(col.key)
    })
})

const handleTableAction = async (event, row) => {
    if (event === "bind") {
        bindCredentialsModalVisible.value = true;
        currentRow.value = row;
    } else if (event === "delete") {
        try {
            const res = await del(row.connectionUuid);
            if (res.status === "200") {
                message.success("连接删除成功");
                fetchConnections(); // 刷新连接列表
            } else {
                message.error(res.message || "连接删除失败");
            }
        } catch (error) {
            message.error("请求删除连接时出错");
        }
    }
};

const connectionsData = ref([]);
const fetchConnections = async () => {
    try {
        await list().then(res => {
            if (res.status === '200') {
                connectionsData.value = res.data
            } else {
                message.error(res.message || '获取密钥列表失败');
            }
        })
    } catch (error) {
        message.error(error.response.data.message || '请求密钥列表时出错');
    }
};
onMounted(() => {
    loading();
    fetchConnections();
});
</script>
<style>
.columns {
    font-size: 14px;
    font-family: ui-sans-serif, -apple-system, system-ui
}

.view-header {
    display: flex;
    justify-content: space-between;
    gap: 20px;
    align-items: center;

    .main-area {
        display: flex;
        gap: 20px;
        align-items: center;
    }

    .other-area {
        display: flex;
        gap: 8px;
        align-items: center;
    }
}
</style>
