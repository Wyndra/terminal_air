<template>
    <div class="add_border">
        <n-h4 style="margin: 0px;cursor: pointer;" @click="handleTaggleConnect">{{ connectInfoValue.connectName }}</n-h4>
        <div style="flex: 1;"></div>
        <n-popover :overlap="overlap" placement="right-start" trigger="hover">
            <template #trigger>
                <n-icon size="26">
                    <svg t="1714908494826" class="icon" viewBox="0 0 1024 1024" version="1.1"
                        xmlns="http://www.w3.org/2000/svg" p-id="19075" width="200" height="200">
                        <path d="M288 512m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z" fill="#272636" p-id="19076">
                        </path>
                        <path d="M512 512m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z" fill="#272636" p-id="19077">
                        </path>
                        <path d="M736 512m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z" fill="#272636" p-id="19078">
                        </path>
                    </svg>
                </n-icon>
            </template>
            <div>
                <n-flex vertical>
                    <div class="virtual_button" @click="handleEditButton">编辑</div>
                    <!-- <div class="line"></div> -->
                    <div class="virtual_button warn_button" @click="openDeleteWindows">删除</div>
                </n-flex>
            </div>
        </n-popover>
    </div>
</template>
<script setup>
import { defineEmits, onMounted, ref, defineProps } from 'vue';
import { del } from '@/api/connect';
import { useStore } from 'vuex';
import { useMessage,useDialog } from 'naive-ui';

const store = useStore();
const message = useMessage();
const dialog = useDialog();
const hasShownError = ref(store.getters.hasShownError);

const emit = defineEmits(["taggle_connect","refresh_connection_list"]); //声明 emits

const handleTaggleConnect = () => {
    emit("taggle_connect", connectInfoValue);
};

const handleEditButton = () => {
    store.state.showEditConnectionDrawer = true;
    store.state.editConnectionInfo = connectInfoValue.value;
};

const openDeleteWindows = () => {
    dialog.warning({
        showIcon: false,
        title: "确认删除",
        content: "操作不可撤销，确定继续吗？",
        positiveText: "删除",
        negativeText: "取消",
        onPositiveClick: () => {
            handleDelete();
        },
        onNegativeClick: () => {
            message.info("已取消操作");
        }
    });
}

async function deleteConnection() {
    try {
        const tdata = {
            cid: connectInfoValue.value.cid
        };
        const res = await del(tdata);
        if (res.status === '200') {
            message.success('删除成功');
            store.commit('setHasShownError', false); // 更新 Vuex 状态，标记未显示错误
            hasShownError.value = false; // 本地更新，避免重复弹出
            // 删除成功后，重新获取连接列表
            emit("refresh_connection_list");
        } else {
            // 只在还未显示错误的情况下弹出错误
            if (!hasShownError.value) {
                message.error(res.message || '删除连接失败');
                store.commit('setHasShownError', true); // 更新 Vuex 状态，标记已显示错误
                hasShownError.value = true; // 本地更新，避免重复弹出
            }
        }
    } catch (error) {
        // 只在还未显示错误的情况下弹出错误
        if (!hasShownError.value) {
            message.error('删除连接失败');
            store.commit('setHasShownError', true);
            hasShownError.value = true;
        }
    }
}


const handleDelete = () => {
    deleteConnection();
};

const props = defineProps({
    connectInfoValue: Object,
});

const connectInfoValue = ref(props.connectInfoValue);


onMounted(() => {
    console.log("[ConnectionItem]",props.connectInfoValue);
});

</script>
<style scoped>
.add_border {
    display: flex;
    text-align: left;
    background-color: #ffffff;
    padding: 10px;
    border-radius: 8px;
    margin-bottom: 5px;
}

.add_border:hover {
    background-color: #edf1f2 !important;
    border-radius: 8px;
}

.line {
    border-bottom: 1px solid #e5e5e5;
}
.warn_button {
    color: #ff4d4f;
}

.virtual_button {
    padding: 5px 10px;
    border-radius: 5px;
    cursor: pointer;
    font-weight: 400;
}

.warn_button:hover {
    color: #ff4d4f;
}
</style>