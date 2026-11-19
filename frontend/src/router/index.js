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
      { path: 'admin/users', component: () => import('../views/admin/AdminUser.vue'), meta: { title: '用户管理', admin: true } },
      { path: 'admin/configs', component: () => import('../views/admin/AdminConfig.vue'), meta: { title: '系统参数', admin: true } },
      { path: 'admin/notices', component: () => import('../views/admin/AdminNotice.vue'), meta: { title: '公告管理', admin: true } },
      { path: 'admin/logs', component: () => import('../views/admin/AdminLog.vue'), meta: { title: '操作日志', admin: true } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // 登录页与注册页无需登录态
  if (to.meta.public) {
    next()
    return
  }
  // 没有令牌直接送回登录页
  if (!getToken()) {
    next('/login')
    return
  }
  // 管理页要求角色为管理员。
  // 这一层只是避免普通用户看到不该看的界面，接口权限仍由后端拦截器把关。
  if (to.meta.admin && !isAdmin()) {
    ElMessage.error('该页面仅管理员可访问')
    next('/home')
    return
  }
  next()
})

export default router
