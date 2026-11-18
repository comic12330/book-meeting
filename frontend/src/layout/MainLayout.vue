<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">会议室预定系统</div>
      <el-menu :default-active="activeMenu" router background-color="#2b3648" text-color="#c0c4cc"
               active-text-color="#ffffff">
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>系统首页</span>
        </el-menu-item>
        <el-menu-item index="/rooms">
          <el-icon><OfficeBuilding /></el-icon>
          <span>会议室预定</span>
        </el-menu-item>
        <el-menu-item index="/my-books">
          <el-icon><Tickets /></el-icon>
          <span>我的预定</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人资料</span>
        </el-menu-item>
        <template v-if="isAdminUser">
          <el-menu-item index="/admin/rooms">
            <el-icon><Setting /></el-icon>
            <span>会议室管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/books">
            <el-icon><DocumentChecked /></el-icon>
            <span>预定审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/notices">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="crumb">{{ currentTitle }}</span>
        <div class="right">
          <span class="user">{{ user.realName }}（{{ isAdminUser ? '管理员' : '普通用户' }}）</span>
          <el-button link type="primary" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getUser, clearAuth, isAdmin } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const user = computed(() => getUser() || {})
const isAdminUser = computed(() => isAdmin())
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')

async function logout() {
  await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
  clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #2b3648;
}

.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #ffffff;
  font-size: 15px;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.aside :deep(.el-menu) {
  border-right: none;
}

.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
}

.crumb {
  font-size: 15px;
  color: #303133;
}

.right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user {
  font-size: 13px;
  color: #606266;
}

.main {
  padding: 0;
  background: #f5f7fa;
}
</style>
