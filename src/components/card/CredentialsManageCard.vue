<template>
    <n-card title="凭证中心" style="min-width: 1000px;max-height: 625px;"
        content-style="display: flex; flex-direction: column;gap: 20px;">
        <template #header-extra>
            <n-icon :size="20">
                <QuestionCircle32Regular />
            </n-icon>
            了解更多
        </template>
        <div class="view-header">

            <div class="main-area">
                <n-button type="primary" style="width: 120px;"
                    @click="createCredentialsModalVisible = true">创建凭证</n-button>
            </div>
            <div class="other-area">
                <n-button circle size="small" @click="() => {
                    fetchCredentials();
                    loading();
                    message.success('刷新成功');
                }">
                    <n-icon>
                        <RefreshSharp />
                    </n-icon>
                </n-button>
            </div>

        </div>
        <n-spin size="large" :show="onloading">
            <n-data-table :columns="credentialsColumns" :data="credentialsData" :bordered="false">
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
import { ref, onMounted, h } from 'vue';
import { listCredentials, deleteCredentials } from '@/api/credentials';
import { useMessage, NTag, NButton, NPopconfirm, NDataTable } from "naive-ui";
import { QuestionCircle32Regular } from '@vicons/fluent';
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
            const status = row.status;
            return h(NTag, {
                type: status === 0 ? 'info' : status === 1 ? 'warning' : 'success',
                size: "small",
            }, status === 0 ? '未绑定' : status === 1 ? '服务端绑定' : '绑定成功');
        }
    },
    {
        title: '凭证标签',
        key: 'tags',
        align: 'center',
        className: 'columns',
        // tags返回的是以逗号分隔的字符串，渲染成标签
        render(row) {
            const tags = row.tags.split(',');
            if (row.tags === '') {
                return h('div', '');
            }
            return h('div', tags.map(tag => {
                return h(NTag, { type: 'primary', size: "small", style: { marginRight: '4px' } }, tag);
            }));
        }
    },
    {
        title: '凭证创建时间',
        key: 'createTime',
        className: 'columns',
    },
    {
        title: "操作",
        className: "columns",
        align: "center",
        render(row) {
            return h(TableActions, {
                row,
                actions: [
                    { label: "绑定", event: "bind", type: "primary", quaternary: true },
                    { label: "删除", event: "delete", type: "error", confirm: true, confirmText: "确定要删除该凭证吗？" }
                ],
                onAction: (event, row) => handleTableAction(event, row)
            });
        }
    }
];

const handleTableAction = async (event, row) => {
    if (event === "bind") {
        bindCredentialsModalVisible.value = true;
        currentRow.value = row;
    } else if (event === "delete") {
        try {
            const res = await deleteCredentials(row.id);
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

function formatDate(input) {
    const [year, month, dayTime] = input.split("/");
    const [day, time] = dayTime.split(" ");
    return `${year}-${month.padStart(2, "0")}-${day.padStart(2, "0")} ${time}`;
}

const credentialsData = ref([]);
const fetchCredentials = async () => {
    try {
        const res = await listCredentials();
        if (res.status === '200') {
            credentialsData.value = res.data
            credentialsData.value.forEach(item => {
                // 时间转换
                item.createTime = formatDate(new Date(item.createTime).toLocaleString())
            });
        } else {
            message.error(res.message || '获取密钥列表失败');
        }
    } catch (error) {
        message.error('请求密钥列表时出错');
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
        gap: 20px;
        align-items: center;
    }
}
</style>
