<template>
    <div>
        <template v-for="(action, index) in actions" :key="index">
            <n-button v-if="!action.confirm" :type="action.type || 'default'" :size="action.size || 'small'"
                :quaternary="action.quaternary || false" @click="() => emitAction(action.event)">
                {{ action.label }}
            </n-button>

            <n-popconfirm v-else trigger="click" placement="top" :negative-text="action.negativeText || '取消'"
                :positive-text="action.positiveText || '确定'" @positive-click="() => emitAction(action.event)">
                <template #trigger>
                    <n-button :type="action.type || 'error'" :size="action.size || 'small'" quaternary>
                        {{ action.label }}
                    </n-button>
                </template>
                <div>{{ action.confirmText || '确定要执行此操作吗？' }}</div>
            </n-popconfirm>
        </template>
    </div>
</template>

<script setup>
import { defineProps, defineEmits } from "vue";

const props = defineProps({
    row: Object,  // 当前行数据
    actions: Array // 操作列表 [{ label: "删除", event: "delete", type: "error", confirm: true }]
});

const emit = defineEmits(["action"]);

// 触发操作事件
const emitAction = (event) => {
    emit("action", event, props.row);
};
</script>
