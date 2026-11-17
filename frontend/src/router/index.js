import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getToken, isAdmin } from '../utils/auth'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('../views/Register.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', component: () => import('../views/Home.vue'), meta: { title: '系统首页' } },
      { path: 'rooms', component: () => import('../views/RoomList.vue'), meta: { title: '会议室预定' } },
      { path: 'my-books', component: () => import('../views/MyBooks.vue'), meta: { title: '我的预定' } },
      { path: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人资料' } },
      { path: 'admin/rooms', component: () => import('../views/admin/AdminRoom.vue'), meta: { title: '会议室管理', admin: true } },
      { path: 'admin/books', component: () => import('../views/admin/AdminBook.vue'), meta: { title: '预定审核', admin: true } },
      { path: 'admin/notices', component: () => import('../views/admin/AdminNotice.vue'), meta: { title: '公告管理', admin: true } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.public) {
    next()
    return
  }
  if (!getToken()) {
    next('/login')
    return
  }
  if (to.meta.admin && !isAdmin()) {
    ElMessage.error('该页面仅管理员可访问')
    next('/home')
    return
  }
  next()
})

export default router
