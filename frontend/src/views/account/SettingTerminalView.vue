<template>
    <n-card title="终端设置" style="min-width: 1000px;max-height: 625px;height: 100vh">
        <template #header-extra>
            <div style="display: flex;align-items: center;">
                <n-icon :size="18">
                    <QuestionCircle32Regular />
                </n-icon>
                <span> 了解 终端 必读</span>
            </div>
        </template>
    </n-card>
</template>
<script setup>
import { ref, h } from 'vue';
import { QuestionCircle32Regular } from '@vicons/fluent';
</script>