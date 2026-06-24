import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { noAuth: true } },
  { path: '/', redirect: '/talent/list' },
  { path: '/talent/list', name: 'TalentList', component: () => import('../views/TalentList.vue') },
  { path: '/talent/add', name: 'TalentAdd', component: () => import('../views/TalentEdit.vue') },
  { path: '/talent/edit/:id', name: 'TalentEdit', component: () => import('../views/TalentEdit.vue') },
  { path: '/attribute/list', name: 'AttributeList', component: () => import('../views/AttributeList.vue') },
  { path: '/screening/plans', name: 'ScreeningPlans', component: () => import('../views/ScreeningPlans.vue') },
  { path: '/scoring/plans', name: 'ScoringPlans', component: () => import('../views/ScoringPlans.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!to.meta.noAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
