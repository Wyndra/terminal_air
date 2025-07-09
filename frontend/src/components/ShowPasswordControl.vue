<template>
  <div class="flex flex-row items-center justify-center">
    <div v-if="!props.text" class="flex gap-1">
      <span v-for="n in 8" :key="n" class="inline-block w-2.5 h-2.5 bg-gray-400 rounded-full"></span>
    </div>
    <div v-else class="flex gap-1 items-center">
      <template v-if="!currentState">
        <span v-for="n in (props.text?.length || 8)" :key="n" class="inline-block w-1 h-1 bg-gray-500 rounded-full"></span>
      </template>
      <template v-else>
        <span class="text-gray-500 select-none">{{ props.text }}</span>
      </template>
    </div>
    <n-tooltip trigger="hover" placement="top">
      <template #trigger>
        <n-icon
          class="ml-2 cursor-pointer"
          size="16"
          color="#888888"
          :component="currentState ? EyeOffOutline : EyeOutline"
          @mousedown="currentState = true"
          @mouseup="currentState = false"
          @mouseleave="currentState = false"
        />
      </template>
      {{ currentState ? '隐藏密码' : '显示密码' }}
    </n-tooltip>
  </div>
</template>
<script setup>
import { defineProps, ref } from "vue";
import { EyeOutline, EyeOffOutline } from '@vicons/ionicons5';

const props = defineProps({
  text: String, // 显示的文本（密码）
});

const currentState = ref(false); // false: 隐藏，true: 显示
</script>