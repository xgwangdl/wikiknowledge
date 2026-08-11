import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      { path: '', redirect: '/chat' },
      { path: 'chat', component: () => import('../views/ChatView.vue') },
      { path: 'knowledge-bases', component: () => import('../views/KnowledgeBasesView.vue') },
      { path: 'eval', component: () => import('../views/EvalView.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('accessToken')
  if (!token && to.path !== '/login') return '/login'
  if (token && to.path === '/login') return '/chat'
})

export default router
