import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 路由组件
import store from './store' // 状态管理，pinia
import naive from 'naive-ui/es/preset' // 引入 Naive UI 预设
import "@/assets/tailwind.css" // 引入 Tailwind CSS
// 挂载
createApp(App).use(naive).use(store).use(router).mount('#app')
