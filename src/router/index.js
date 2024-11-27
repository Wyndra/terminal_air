import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/ProfileView.vue'),
  },
  {
    path: '/privacy-policy',
    name: 'privacy-policy',
    component: () => import('../views/PrivacyPolicyView.vue'),
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('token') !== null; // 检查本地存储中的登录状态
  // TODO 判断token是否过期
  

  // if (to.matched.some(record => record.meta.requiresAuth) && !isLoggedIn) {
  //   // next({
  //   //   path: '/',
  //   //   query: { redirect: to.fullPath } // 将当前路由全路径作为redirect参数传递
  //   // })
  //   next()
  // } else {
  //   next()
  // }
  if (!isLoggedIn) {
    next()
  }else{
    next()
  }
})

export default router
