<template ref="appRef">
  <n-config-provider :theme="theme" :hljs="hljs">
    <n-dialog-provider>
      <n-notification-provider>
        <n-message-provider>
          <router-view />
          <terminal-settings-drawer />
        </n-message-provider>
      </n-notification-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup>
import TerminalSettingsDrawer from '@/components/drawer/TerminalSettingsDrawer.vue';
import hljs from 'highlight.js';
import { ref, watch, computed } from 'vue';
import { useStore } from 'vuex';
import { lightTheme, darkTheme, NThemeEditor,useThemeVars } from 'naive-ui'
import { accessibleDarkTheme } from "@/constant/systemTheme.js";

const theme = ref();
const themeVars = useThemeVars();

console.log("当前主题变量", themeVars.value);


// theme.value.bodyColor = "#171D38";


const store = useStore();

const currentTheme = computed(() => store.state.theme)

watch(currentTheme, (newTheme) => {
  if (newTheme === 'dark') {
    localStorage.setItem('theme', 'dark');
    // theme.value = accessibleDarkTheme // 使用自定义深色主题或默认
    theme.value = darkTheme // 使用自定义深色主题或默认
  } else {
    localStorage.setItem('theme', 'light');
    theme.value = lightTheme // 使用自定义浅色主题或默认
  }
}, { immediate: true })


</script>

<style lang="scss">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  // color: #2c3e50;
  overflow: hidden;
}
</style>
