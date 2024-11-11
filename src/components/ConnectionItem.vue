<template>
    <div class="add_border" @click="handleTaggleConnect">
        <n-h4 style="margin: 0px;">{{ connectInfoValue.connectName }}</n-h4>
        <div style="flex: 1;"></div>
        <n-icon size="26">
            <svg t="1714908494826" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
                p-id="19075" width="200" height="200">
                <path d="M288 512m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z" fill="#272636" p-id="19076">
                </path>
                <path d="M512 512m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z" fill="#272636" p-id="19077">
                </path>
                <path d="M736 512m-64 0a64 64 0 1 0 128 0 64 64 0 1 0-128 0Z" fill="#272636" p-id="19078">
                </path>
            </svg>
        </n-icon>
    </div>
</template>
<script setup>
import { defineEmits, onMounted, ref, defineProps } from 'vue';

const emit = defineEmits(["taggle_connect"]); //声明 emits
const handleTaggleConnect = () => {
    emit("taggle_connect", connectInfoValue);
};

const props = defineProps({
    connectInfoValue: Object,
});

const connectInfoValue = ref(props.connectInfoValue);

onMounted(() => {
    console.log(props.connectInfoValue);
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
    padding-top: 8%;
    margin-bottom: 8%;
    border-bottom: 1px solid #e5e5e5;
}
</style>