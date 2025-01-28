<template>
    <div class="verify-code-container">
        <input v-for="(item, index) in inputs" :key="index" type="text" v-model="inputs[index]" maxlength="1"
            @input="onInput(index, $event)" @keydown="onKeydown(index, $event)" class="verify-code-input" />
    </div>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from "vue";

const props = defineProps({
    modelValue: String
});
const emit = defineEmits(['update:modelValue']);

const inputs = ref(new Array(6).fill(""));

const onInput = (index, e) => {
    const value = e.target.value;
    if (value.length > 1) {
        e.target.value = value.slice(0, 1);
    }
    if (value.match(/[^0-9]/)) {
        e.target.value = "";
    }
    inputs.value[index] = e.target.value;

    if (value.length === 1 && index < inputs.value.length - 1) {
        const nextInput = e.target.nextElementSibling;
        if (nextInput) {
            nextInput.focus();
        }
    }
    updateCode();
};

const onKeydown = (index, e) => {
    if (e.key === "Backspace" && !e.target.value && index > 0) {
        const prevInput = e.target.previousElementSibling;
        if (prevInput) {
            prevInput.focus();
        }
    }
};

const updateCode = () => {
    const code = inputs.value.join("");
    // 只在实际值变化时触发 emit，避免过多的更新
    if (code !== props.modelValue) {
        emit('update:modelValue', code);
    }
};

watch(() => props.modelValue, (newValue) => {
    // 只有当 modelValue 和 inputs 不一致时才更新 inputs
    if (newValue && newValue !== inputs.value.join("")) {
        inputs.value = newValue.split('').slice(0, 6);
    }
}, { immediate: true });

watch(inputs, updateCode, { deep: true });
</script>

<style scoped>
.verify-code-container {
    display: flex;
    justify-content: center;
    gap: 8px;
}

.verify-code-input {
    width: 40px;
    height: 40px;
    font-size: 20px;
    text-align: center;
    border: 0.8px solid #ccc;
    border-radius: 4px;
    outline: none;
    caret-color: transparent;
    transition: border-color 0.3s;
    background-color: #f4f4f4;
    font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

.verify-code-input:focus {
    border-color: #319154;
    box-shadow: 0 0 5px rgba(49, 145, 84, 0.5);
}
</style>
