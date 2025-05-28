import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页 - FUFU智能体应用平台',
    }
  },
  {
    path: '/fufu-master',
    name: 'FufuMaster',
    component: () => import('../views/FufuMaster.vue'),
    meta: {
      title: '智能体应用平台',
      description: '智能体顾问，帮你解答各种问题'
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'AI智能体 - FUFU智能体应用平台',
      description: 'AI智能体是FUFU智能体应用平台的全能助手，能解答各类专业问题，提供不那麽精准建议和解决方案'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局导航守卫，设置文档标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router 