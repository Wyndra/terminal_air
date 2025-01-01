<template>
  <n-config-provider :theme="theme">
    <n-dialog-provider>
      <n-message-provider>
        <router-view />
        <terminal-settings-drawer />
      </n-message-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup>
import TerminalSettingsDrawer from '@/components/TerminalSettingsDrawer.vue';
// ... 其他代码保持不变
</script>

<style lang="scss">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  overflow: hidden;
}
</style>
