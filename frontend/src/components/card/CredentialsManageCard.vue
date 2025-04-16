<template>
    <n-card title="凭证中心" style="min-width: 1000px;max-height: 625px;"
        content-style="display: flex; flex-direction: column;gap: 20px;">
        <template #header-extra>
            <div style="display: flex;align-items: center;">
                <n-icon :size="18">
                    <QuestionCircle32Regular />
                </n-icon>
                <span> 了解 凭证 必读</span>
            </div>

        </template>
        <div class="view-header">

            <div class="main-area">
                <n-button type="primary" style="width: 120px;"
                    @click="createCredentialsModalVisible = true">创建凭证</n-button>
            </div>
            <div class="other-area">
                <n-button circle size="small" @click="() => {fetchCredentials();loading();message.success('刷新成功');}">
                    <n-icon>
                        <RefreshSharp />
                    </n-icon>
                </n-button>
                <n-popselect v-model:value="currentCredentialsColumnsOptions" multiple
                    :options="credentialsColumnsOptions" size="medium">
                    <template #header>
                        列展示选择
                    </template>
                    <n-button circle size="small" @click="() => { fetchCredentials(); loading(); }">
                        <n-icon>
                            <Settings48Regular />
                        </n-icon>
                    </n-button>
                </n-popselect>
            </div>

        </div>
        <n-spin size="large" :show="onloading">
            <!-- <n-data-table :columns="credentialsColumns" :data="credentialsData" :bordered="false"> -->
            <n-data-table :columns="filteredColumns" :data="credentialsData" :bordered="false">
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
</template>
<script setup>
import { ref, onMounted, h, computed } from 'vue';
import { listCredentials, deleteCredentials } from '@/api/credentials';
import { useMessage, NTag, NButton, NPopconfirm, NDataTable } from "naive-ui";
import { QuestionCircle32Regular, Settings48Regular } from '@vicons/fluent';
import { RefreshSharp } from "@vicons/ionicons5"
import CreateCredentialsModal from '@/components/modal/CreateCredentialsModal.vue';
import BindCredentialsModal from '@/components/modal/BindCredentialsModal.vue';
import TableActions from '@/components/TableActions.vue';

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


const credentialsColumns = [
    {
        title: '凭证名称',
        key: 'name',
        className: 'columns',
    },
    {
        title: '凭证指纹',
        key: 'fingerprint',
        className: 'columns',
    },
    {
        title: '凭证状态',
        key: 'status',
        align: 'center',
        className: 'columns',
        render(row) {
            const status = row.status
            return h(
                NTag,
                {
                    type: status === 0 ? 'info' : status === 1 ? 'warning' : 'success',
                    size: 'small',
                },
                () => (status === 0 ? '未绑定' : status === 1 ? '服务端绑定' : '绑定成功')
            )
        },
    },
    {
        title: '凭证标签',
        key: 'tags',
        align: 'center',
        className: 'columns',
        render(row) {
            if (!row.tags) return h('div', '')
            const tags = row.tags.split('|') 
            return h(
                'div',
                tags.map((tag) =>
                    h(
                        NTag,
                        {
                            type: 'primary',
                            size: 'small',
                            style: { marginRight: '4px' },
                        },
                        () => tag
                    )
                )
            )
        },
    },
    {
        title: '凭证创建时间',
        key: 'createTime',
        className: 'columns',
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
                        confirmText: '确定要删除该凭证吗？',
                    },
                ],
                onAction: (event, row) => handleTableAction(event, row),
            })
        },
    },
]

// 可选列配置（用于列选择器）
const credentialsColumnsOptions = [
    { label: '凭证名称', value: 'name' },
    { label: '凭证指纹', value: 'fingerprint' },
    { label: '凭证状态', value: 'status' },
    { label: '凭证标签', value: 'tags' },
    { label: '凭证创建时间', value: 'createTime' },
    { label: '操作', value: 'actions'}
]

// 当前选中的列（默认全部）
const currentCredentialsColumnsOptions = ref(
    credentialsColumnsOptions.map((item) => item.value)
)

// 实际用于展示的列（响应式计算）
const filteredColumns = computed(() => {
    return credentialsColumns.filter((col) => {
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
            const res = await deleteCredentials(row.uuid);
            if (res.status === "200") {
                message.success("凭证删除成功");
                fetchCredentials();
            } else {
                message.error(res.message || "凭证删除失败");
            }
        } catch (error) {
            message.error("请求删除凭证时出错");
        }
    }
};

const credentialsData = ref([]);
const fetchCredentials = async () => {
    try {
        await listCredentials().then(res => {
            if (res.status === '200') {
                credentialsData.value = res.data
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
    fetchCredentials();
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
