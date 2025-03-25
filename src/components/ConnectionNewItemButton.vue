<template>
    <div class="add_border" @click="handleNewConnect">
        <n-h4 style="margin: 0px;">开启新连接</n-h4>
        <div style="flex: 1;"></div>
        <n-icon size="20" style="align-self: center;">
            <Server></Server>
        </n-icon>
    </div>
    <div class="line"></div>
</template>
<script setup>
import { Server } from '@vicons/fa';
import { defineEmits } from 'vue';
const emit = defineEmits(["new-connection"]); //声明 emits
const handleNewConnect = () => {
    emit("new-connection"); //触发事件
};

</script>
<style scoped>
.add_border {
    display: flex;
    text-align: left;
    background-color: #ebebeb;
    padding: 10px;
    border-radius: 8px;
    border: 0.8px solid rgb(52, 53, 54);
}

.add_border:hover {
    background-color: #dcdcdc !important;
    border-radius: 8px;
}

.line {
    padding-top: 8%;
    margin-bottom: 8%;
    border-bottom: 1px solid #e5e5e5;
}
</style>