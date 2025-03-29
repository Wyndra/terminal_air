<template>
    <n-card title="凭证中心" style="min-width: 1000px;" content-style="display: flex; flex-direction: column;gap: 20px;">
        <div class="view-header">
            <div class="main-area">
                <n-button type="primary" style="width: 120px;" @click="credentialsModalVisible = true">创建凭证</n-button>
            </div>
            <div class="other-area">
                <n-button circle size="small" @click="fetchCredentials">
                    <n-icon>
                        <RefreshSharp />
                    </n-icon>
                </n-button>
            </div>
            
        </div>
        <n-data-table :columns="credentialsColumns" :data="credentialsData" :bordered="false" />
    </n-card>
    <CreateCredentialsModal
        v-model:show="credentialsModalVisible"
        @close="handleClose"
    />
</template>
<script setup>
import { ref, onMounted,h } from 'vue';
import { listCredentials,deleteCredentials } from '@/api/credentials';
import { useMessage,NTag,NButton,NPopconfirm } from "naive-ui";
import {RefreshSharp} from "@vicons/ionicons5"
import CreateCredentialsModal from '@/components/modal/CreateCredentialsModal.vue';

const message = useMessage();
const credentialsModalVisible = ref(false);
const handleClose = () => {
    credentialsModalVisible.value = false;
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
        title: '凭证标签',
        key: 'tags',
        className: 'columns',
        // tags返回的是以逗号分隔的字符串，渲染成标签
        render(row){
            const tags = row.tags.split(',');
            return h('div', tags.map(tag => {
                return h(NTag, { type: 'primary', style: { marginRight: '4px' } }, tag);
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
        render(row) {
            return h("div", [
            h(
                NPopconfirm,
                {
                    trigger: "click",
                    placement: "top",
                    'negative-text': "取消",
                    'positive-text': "确定",
                    onPositiveClick: async () => {
                        // 删除凭证
                        console.log("删除凭证", row);
                        await deleteCredentials(row.id).then((res) => {
                            if (res.status === '200') {
                                message.success("凭证删除成功");
                                // 刷新凭证列表
                                fetchCredentials();
                            } else {
                                message.error(res.message || '凭证删除失败');
                            }
                        }).catch((error) => {
                            message.error('请求删除凭证时出错');
                        });
                    },
                    onNegativeClick: () => {
                        console.log("取消删除");
                    },
                },
                {
                    trigger: () => h(NButton, {
                        type: "warning",
                        size: "small",
                        quaternary: true,
                    }, "删除"),
                    default: () => h("div", "确定要删除该凭证吗？"),
                },
            ),
            ]);
  },
}
];

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
            credentialsData.value = res.data;
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
