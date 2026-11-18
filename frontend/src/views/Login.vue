<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <aside class="brand">
      <div class="brand-top">
        <div class="logo">
          <span class="logo-mark">
            <svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4.5" width="18" height="17" rx="2.5" />
              <path d="M16 2.5v4M8 2.5v4M3 10.5h18" />
            </svg>
          </span>
          <span class="logo-text">会议室预定系统</span>
        </div>

        <h1 class="brand-title">让会议室的使用不再冲突</h1>

        <!-- 装饰：会议室时间槽，静态数据不参与业务 -->
        <div class="room-card">
          <div class="room-head">
            <span class="room-name">会议室</span>
            <span class="status-pill">今日</span>
          </div>
          <div v-for="s in schedule" :key="s.time" class="slot">
            <span class="slot-time">{{ s.time }}</span>
            <span class="slot-bar" :class="s.state"></span>
            <span class="slot-state" :class="s.state">{{ s.label }}</span>
          </div>
        </div>
      </div>

      <div class="brand-foot">© 2026 会议预定系统</div>
    </aside>

    <!-- 右侧表单区 -->
    <main class="form-side">
      <div class="form">
        <h2 class="form-title">欢迎回来</h2>

        <el-form ref="formRef" :model="form" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号">
              <template #prefix>
                <svg class="field-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7.5" r="3.8" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              @keyup.enter="onSubmit"
            >
              <template #prefix>
                <svg class="field-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3.5" y="10.5" width="17" height="10" rx="2.4" />
                  <path d="M7.5 10.5V7a4.5 4.5 0 0 1 9 0v3.5" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">
            登 录
          </el-button>
        </el-form>

        <div class="foot">
          <span>还没有账号？</span>
          <el-button link type="primary" @click="$router.push('/register')">立即注册</el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'
import { setAuth } from '../utils/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

// 账号框预填管理员账号，密码留空由使用者输入
const form = reactive({ username: 'admin', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 左侧装饰卡数据，纯展示
const schedule = [
  { time: '09:00', state: 'busy', label: '已预定' },
  { time: '10:00', state: 'busy', label: '已预定' },
  { time: '11:00', state: 'free', label: '空闲' },
  { time: '14:00', state: 'free', label: '空闲' }
]

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    setAuth(res.data)
    ElMessage.success('登录成功')
    router.push(res.data.role === 1 ? '/admin/books' : '/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
}

/* ==================== 左侧品牌区 ==================== */
.brand {
  position: relative;
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 52px 56px 26px;
  color: #fff;
  background: linear-gradient(140deg, #0f2a5e 0%, #1d4ed8 52%, #3b82f6 100%);
  overflow: hidden;
}

/* 两团光斑，避免纯渐变发闷 */
.brand::before {
  content: '';
  position: absolute;
  width: 460px;
  height: 460px;
  right: -150px;
  top: -140px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.2), transparent 68%);
}

.brand::after {
  content: '';
  position: absolute;
  width: 360px;
  height: 360px;
  left: -120px;
  bottom: -150px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(147, 197, 253, 0.26), transparent 70%);
}

.brand-top {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 452px;
  /* auto 四向吸收剩余空间：在左栏内水平+垂直居中 */
  margin: auto;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 48px;
}

.logo-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
}

.logo-text {
  font-size: 16.5px;
  font-weight: 500;
  letter-spacing: 0.6px;
}

.brand-title {
  margin: 0 0 42px;
  font-size: 32px;
  line-height: 1.5;
  font-weight: 600;
  letter-spacing: 0.8px;
}

/* 装饰卡：会议室时间槽 */
.room-card {
  max-width: 366px;
  padding: 18px 20px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.17);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.room-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.13);
}

.room-name {
  font-size: 13.5px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.status-pill {
  padding: 2px 9px;
  border-radius: 20px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.19);
  color: rgba(255, 255, 255, 0.92);
}

.slot {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 9px 0;
}

.slot + .slot {
  border-top: 1px solid rgba(255, 255, 255, 0.07);
}

.slot-time {
  width: 46px;
  flex: none;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.62);
  font-variant-numeric: tabular-nums;
}

.slot-bar {
  flex: 1;
  height: 8px;
  border-radius: 4px;
}

.slot-bar.busy {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.66), rgba(255, 255, 255, 0.34));
}

.slot-bar.free {
  border: 1px dashed rgba(255, 255, 255, 0.4);
}

.slot-state {
  width: 46px;
  flex: none;
  text-align: right;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.58);
}

.slot-state.free {
  color: rgba(190, 230, 255, 0.82);
}

.brand-foot {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 452px;
  margin: 0 auto;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.48);
  letter-spacing: 0.3px;
}

/* ==================== 右侧表单区 ==================== */
.form-side {
  flex: 0 0 46%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  background: #fff;
}

.form {
  width: 100%;
  max-width: 356px;
}

.form-title {
  margin: 0 0 30px;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0.4px;
  color: #101828;
}

.form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.form :deep(.el-input__wrapper) {
  min-height: 48px;
  padding: 0 14px;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e3e8ef inset;
  transition: box-shadow 0.18s;
}

.form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d0dae8 inset;
}

.form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2563eb inset, 0 0 0 3px rgba(37, 99, 235, 0.11);
}

.form :deep(.el-input__inner) {
  font-size: 14.5px;
}

.form :deep(.el-input__inner::placeholder) {
  color: #b0b8c4;
}

.form :deep(.el-form-item__error) {
  padding-top: 4px;
  font-size: 12px;
}

/* 输入框图标：聚焦时随边框一起变蓝 */
.field-icon {
  color: #a3adbb;
  transition: color 0.18s;
}

.form :deep(.el-input__wrapper.is-focus .field-icon) {
  color: #2563eb;
}

.form :deep(.el-input__password) {
  color: #a3adbb;
  font-size: 17px;
}

.form :deep(.el-input__password:hover) {
  color: #6b7789;
}

.form .submit {
  width: 100%;
  height: 48px;
  margin-top: 8px;
  border: 0;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #2563eb, #1d4ed8);
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.26);
  transition: transform 0.16s, box-shadow 0.16s, background 0.16s;
}

.form .submit:hover {
  transform: translateY(-1px);
  background: linear-gradient(90deg, #2f6cf0, #2255e8);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.32);
}

.form .submit:active {
  transform: translateY(0);
}

.foot {
  margin-top: 22px;
  text-align: center;
  font-size: 13.5px;
  color: #8a94a6;
}

/* ==================== 窄屏：隐藏品牌区 ==================== */
@media (max-width: 960px) {
  .brand {
    display: none;
  }

  .form-side {
    flex: 1;
    padding: 40px 24px;
  }
}
</style>
