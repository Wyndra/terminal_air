import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import store from '../store'
import MainView from '@/views/MainView.vue'

const routes = [
  {
    path: '/',
    name: 'index',
    component: () => import('../views/MainView.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('../views/HomeView.vue'),
      },
      
      {
        path: '/account',
        name: 'account',
        component: () => import('../views/AccountView.vue'),
        children: [
          {
            path: '/account/member',
            name: 'member',
            component: () => import('../components/card/MemberCenterCard.vue'),
          },
          {
            path: '/account/overview',
            name: 'overview',
            component: () => import('../components/card/AccountOverviewCard.vue'),
          },
          {
            path: '/account/credentials',
            name: 'credentials',
            component: () => import('../views/account/CredentialsManageView.vue'),
          },
          {
            path: '/account/termsettings',
            name: 'termSettings',
            component: () => import('../views/account/SettingTerminalView.vue'),
          },
          {
            path: '/account/connection',
            name: 'connectionManage',
            component: () => import('../views/account/ConnectionManageView.vue'),
          }
        ]
      },
      {
        path: '/profile',
        name: 'profile',
        component: () => import('../views/ProfileView.vue'),
      },
    ]
  },
  {
    path: '/privacy-policy',
    name: 'privacy-policy',
    component: () => import('../views/PrivacyPolicyView.vue'),
  },
  {
    path: '/inv',
    name: "SessionRedirect",
    component: () => import('../views/SessionRedirect.vue'),
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = store.getters.isLoggedIn; // 获取登录状态
  // TODO 判断token是否过期
  if (!isLoggedIn) {
    next()
  } else {
    next()
  }
})

export default router
