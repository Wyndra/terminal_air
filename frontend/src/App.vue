<template>
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
