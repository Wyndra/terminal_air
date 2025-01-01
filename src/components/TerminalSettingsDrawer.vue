<template>
    <n-drawer v-model:show="show" :width="500" placement="right" content-style="background:#edf1f2">
        <n-drawer-content closable>
            <template #header>
                <span
                    style="font-size: 20px; align-content: center; font-family: ui-sans-serif, -apple-system, system-ui">
                    终端设置
                </span>
            </template>

            <n-card bordered style="background-color: #fff; height: calc(100vh - 120px);">
                <n-tabs type="line">
                    <!-- 基础设置 -->
                    <n-tab-pane name="basic" tab="基础设置">
                        <n-form :model="settings" label-placement="left" label-width="auto"
                            require-mark-placement="right-hanging" style="margin-top: 16px">
                            <n-form-item label="字体大小">
                                <n-input-number v-model:value="settings.fontSize" :min="8" :max="32" />
                            </n-form-item>
                            <n-form-item label="字体">
                                <n-select v-model:value="settings.fontFamily" :options="fontOptions" filterable tag />
                            </n-form-item>
                            <n-form-item label="行高">
                                <n-input-number v-model:value="settings.lineHeight" :min="1" :max="2" :step="0.1" />
                            </n-form-item>
                            <n-form-item label="字间距">
                                <n-input-number v-model:value="settings.letterSpacing" :min="0" :max="5" :step="0.5" />
                            </n-form-item>
                            <n-form-item label="光标样式">
                                <n-select v-model:value="settings.cursorStyle" :options="cursorStyleOptions" />
                            </n-form-item>
                            <n-form-item label="光标闪烁">
                                <n-switch v-model:value="settings.cursorBlink" />
                            </n-form-item>
                        </n-form>
                    </n-tab-pane>

                    <!-- 主题设置 -->
                    <n-tab-pane name="theme" tab="主题设置">
                        <n-form :model="settings.theme" label-placement="left" label-width="auto"
                            require-mark-placement="right-hanging" style="margin-top: 16px">
                            <n-form-item label="预设主题">
                                <n-select v-model:value="currentTheme" :options="themeOptions" />
                            </n-form-item>
                            <n-divider>自定义主题</n-divider>
                            <n-form-item label="前景色">
                                <n-color-picker v-model:value="settings.theme.foreground" />
                            </n-form-item>
                            <n-form-item label="背景色">
                                <n-color-picker v-model:value="settings.theme.background" />
                            </n-form-item>
                            <n-form-item label="光标颜色">
                                <n-color-picker v-model:value="settings.theme.cursor" />
                            </n-form-item>
                            <n-form-item label="选中背景">
                                <n-color-picker v-model:value="settings.theme.selection" />
                            </n-form-item>
                            <n-form-item label="黑色">
                                <n-color-picker v-model:value="settings.theme.black" />
                            </n-form-item>
                            <n-form-item label="红色">
                                <n-color-picker v-model:value="settings.theme.red" />
                            </n-form-item>
                            <n-form-item label="绿色">
                                <n-color-picker v-model:value="settings.theme.green" />
                            </n-form-item>
                            <n-form-item label="黄色">
                                <n-color-picker v-model:value="settings.theme.yellow" />
                            </n-form-item>
                            <n-form-item label="蓝色">
                                <n-color-picker v-model:value="settings.theme.blue" />
                            </n-form-item>
                            <n-form-item label="紫色">
                                <n-color-picker v-model:value="settings.theme.magenta" />
                            </n-form-item>
                            <n-form-item label="青色">
                                <n-color-picker v-model:value="settings.theme.cyan" />
                            </n-form-item>
                            <n-form-item label="白色">
                                <n-color-picker v-model:value="settings.theme.white" />
                            </n-form-item>
                        </n-form>
                    </n-tab-pane>

                    <!-- 导入导出 -->
                    <n-tab-pane name="import" tab="导入导出">
                        <n-space vertical style="margin-top: 16px">
                            <n-upload accept=".json,.itermcolors" :show-file-list="false" @change="handleFileUpload">
                                <n-button>导入配置文件</n-button>
                            </n-upload>
                            <n-button @click="exportSettings">导出当前配置</n-button>
                            <n-button @click="resetSettings" type="warning">恢复默认设置</n-button>
                        </n-space>
                    </n-tab-pane>
                </n-tabs>
            </n-card>
        </n-drawer-content>
    </n-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useStore } from 'vuex';
import { useMessage } from 'naive-ui';
import plist from 'plist';
import { Close } from '@vicons/ionicons5';

const store = useStore();
const message = useMessage();

const show = computed({
  get: () => store.state.showTerminalSettings,
  set: (value) => store.commit('setShowTerminalSettings', value)
});

const settings = ref({ ...store.state.terminalSettings });
const currentTheme = ref('dark');

const fontOptions = [
  { label: 'Menlo', value: 'Menlo' },
  { label: 'Monaco', value: 'Monaco' },
  { label: 'Courier New', value: 'Courier New' },
  { label: 'Consolas', value: 'Consolas' },
  { label: 'Fira Code', value: 'Fira Code' },
  { label: 'Source Code Pro', value: 'Source Code Pro' },
  { label: 'JetBrains Mono', value: 'JetBrains Mono' },
  { label: 'Cascadia Code', value: 'Cascadia Code' },
  { label: 'Ubuntu Mono', value: 'Ubuntu Mono' },
  { label: 'Hack', value: 'Hack' }
];

const cursorStyleOptions = [
  { label: '块状', value: 'block' },
  { label: '下划线', value: 'underline' },
  { label: '竖线', value: 'bar' }
];

const themeOptions = [
  {
    label: '深色',
    value: 'dark',
    theme: {
      foreground: "#ECECEC",
      background: "#000000",
      cursor: "#FFFFFF",
      selection: "#4040ff",
      black: "#000000",
      red: "#ff0000",
      green: "#33ff00",
      yellow: "#ffff00",
      blue: "#0066ff",
      magenta: "#cc00ff",
      cyan: "#00ffff",
      white: "#d0d0d0"
    }
  },
  {
    label: '浅色',
    value: 'light',
    theme: {
      foreground: "#2c3e50",
      background: "#ffffff",
      cursor: "#2c3e50",
      selection: "#b2d7fe",
      black: "#000000",
      red: "#cc0000",
      green: "#4e9a06",
      yellow: "#c4a000",
      blue: "#3465a4",
      magenta: "#75507b",
      cyan: "#06989a",
      white: "#d3d7cf"
    }
  }
];

watch(currentTheme, (value) => {
  const theme = themeOptions.find(t => t.value === value);
  if (theme) {
    settings.value.theme = { ...theme.theme };
  }
});

watch(settings, (newSettings) => {
  store.commit('updateTerminalSettings', newSettings);
}, { deep: true });

const handleFileUpload = (options) => {
  const file = options.file.file;
  if (!file) return;

  const reader = new FileReader();
  
  reader.onload = (e) => {
    try {
      if (file.name.endsWith('.json')) {
        const config = JSON.parse(e.target.result);
        if (config['Ansi 0 Color']) {
          const theme = parseITerm2JsonTheme(config);
          if (theme) {
            settings.value.theme = theme;
            message.success('iTerm2 配置导入成功');
          }
          return;
        }
        settings.value = { ...settings.value, ...config };
        message.success('配置导入成功');
      } else if (file.name.endsWith('.itermcolors')) {
        const theme = parseITerm2Theme(e.target.result);
        if (theme) {
          settings.value.theme = theme;
          message.success('iTerm2 主题导入成功');
        }
      }
    } catch (error) {
      message.error('配置导入失败：' + error.message);
    }
  };

  reader.onerror = () => {
    message.error('文件读取失败');
  };

  reader.readAsText(file);
};

const exportSettings = () => {
  const config = JSON.stringify(settings.value, null, 2);
  const blob = new Blob([config], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'terminal-settings.json';
  a.click();
  URL.revokeObjectURL(url);
  message.success('配置导出成功');
};

const resetSettings = () => {
  settings.value = { ...store.state.terminalSettings };
  message.success('设置已重置');
};

const parseITerm2Theme = (content) => {
  try {
    const data = plist.parse(content);
    
    const convertColor = (color) => {
      const r = Math.round(color['Red Component'] * 255);
      const g = Math.round(color['Green Component'] * 255);
      const b = Math.round(color['Blue Component'] * 255);
      return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
    };

    return {
      foreground: convertColor(data['Foreground Color']),
      background: convertColor(data['Background Color']),
      cursor: convertColor(data['Cursor Color']),
      selection: convertColor(data['Selection Color']),
      black: convertColor(data['Ansi 0 Color']),
      red: convertColor(data['Ansi 1 Color']),
      green: convertColor(data['Ansi 2 Color']),
      yellow: convertColor(data['Ansi 3 Color']),
      blue: convertColor(data['Ansi 4 Color']),
      magenta: convertColor(data['Ansi 5 Color']),
      cyan: convertColor(data['Ansi 6 Color']),
      white: convertColor(data['Ansi 7 Color']),
      brightBlack: convertColor(data['Ansi 8 Color']),
      brightRed: convertColor(data['Ansi 9 Color']),
      brightGreen: convertColor(data['Ansi 10 Color']),
      brightYellow: convertColor(data['Ansi 11 Color']),
      brightBlue: convertColor(data['Ansi 12 Color']),
      brightMagenta: convertColor(data['Ansi 13 Color']),
      brightCyan: convertColor(data['Ansi 14 Color']),
      brightWhite: convertColor(data['Ansi 15 Color'])
    };
  } catch (error) {
    console.error('解析 iTerm2 主题文件失败:', error);
    message.error('解析 iTerm2 主题文件失败');
    return null;
  }
};

const parseITerm2JsonTheme = (data) => {
  try {
    const convertColor = (color) => {
      if (!color) return null;
      const r = Math.round(color['Red Component'] * 255);
      const g = Math.round(color['Green Component'] * 255);
      const b = Math.round(color['Blue Component'] * 255);
      return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
    };

    return {
      foreground: convertColor(data['Foreground Color']),
      background: convertColor(data['Background Color']),
      cursor: convertColor(data['Cursor Color']),
      selection: convertColor(data['Selection Color']),
      black: convertColor(data['Ansi 0 Color']),
      red: convertColor(data['Ansi 1 Color']),
      green: convertColor(data['Ansi 2 Color']),
      yellow: convertColor(data['Ansi 3 Color']),
      blue: convertColor(data['Ansi 4 Color']),
      magenta: convertColor(data['Ansi 5 Color']),
      cyan: convertColor(data['Ansi 6 Color']),
      white: convertColor(data['Ansi 7 Color']),
      brightBlack: convertColor(data['Ansi 8 Color']),
      brightRed: convertColor(data['Ansi 9 Color']),
      brightGreen: convertColor(data['Ansi 10 Color']),
      brightYellow: convertColor(data['Ansi 11 Color']),
      brightBlue: convertColor(data['Ansi 12 Color']),
      brightMagenta: convertColor(data['Ansi 13 Color']),
      brightCyan: convertColor(data['Ansi 14 Color']),
      brightWhite: convertColor(data['Ansi 15 Color'])
    };
  } catch (error) {
    console.error('解析 iTerm2 JSON 配置文件失败:', error);
    message.error('解析 iTerm2 JSON 配置文件失败');
    return null;
  }
};
</script>

<style scoped>
:deep(.n-drawer-content) {
  padding: 0;
}

:deep(.n-card) {
  height: calc(100vh - 120px);
}

:deep(.n-card-header) {
  padding: 16px 24px;
}

:deep(.n-card-content) {
  padding: 0 24px;
}

:deep(.n-card-footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--n-border-color);
}

:deep(.n-divider) {
  margin: 16px 0;
}

:deep(.n-tabs-nav) {
  padding: 0;
}

:deep(.n-form-item) {
  margin-bottom: 18px;
}

/* 添加抗锯齿和子像素渲染 */
:deep(.xterm) {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style> 