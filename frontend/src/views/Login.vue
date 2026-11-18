<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <section class="brand">
      <div class="brand-inner">
        <div class="logo">
          <span class="logo-mark">
            <el-icon :size="20"><Calendar /></el-icon>
          </span>
          <span class="logo-text">会议室预定系统</span>
        </div>

        <h1 class="brand-title">
          让每一间会议室<br />都在正确的时间被使用
        </h1>
        <p class="brand-desc">
          在线查看空闲时段、一键提交预定、管理员统一审核，<br />
          时间冲突由系统自动拦截。
        </p>

        <!-- 装饰图形：时间槽占用示意，呼应业务 -->
        <div class="schedule">
          <div class="sc-head">
            <span class="sc-room">A301 · 三楼会议室</span>
            <span class="sc-tag">今日</span>
          </div>
          <div
            v-for="s in schedule"
            :key="s.time"
            class="sc-row"
          >
            <span class="sc-time">{{ s.time }}</span>
            <span class="sc-bar" :class="s.state"></span>
            <span class="sc-state">{{ s.label }}</span>
          </div>
        </div>
      </div>

      <div class="brand-foot">© 2026 会议预定系统 · 项目综合实践</div>
    </section>

    <!-- 右侧表单区 -->
    <section class="form-side">
      <div class="form-box">
        <h2 class="form-title">欢迎回来</h2>
        <p class="form-sub">请使用系统账号登录</p>

        <el-form ref="formRef" :model="form" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
              @keyup.enter="onSubmit"
            />
          </el-form-item>
          <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">
            登 录
          </el-button>
        </el-form>

        <div class="footer">
          <span>还没有账号？</span>
          <el-button link type="primary" @click="$router.push('/register')">立即注册</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Calendar } from '@element-plus/icons-vue'
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

// 装饰用静态数据，不参与业务逻辑
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
  height: 100vh;
  display: flex;
  overflow: hidden;
}

/* ============ 左侧品牌区 ============ */
.brand {
  position: relative;
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px 56px 28px;
  color: #fff;
  background: linear-gradient(135deg, #1e3a8a 0%, #1d4ed8 48%, #3b82f6 100%);
  overflow: hidden;
}

/* 两团光斑，让纯色渐变不死板 */
.brand::before,
.brand::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.brand::before {
  width: 420px;
  height: 420px;
  right: -140px;
  top: -120px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.22), transparent 68%);
}

.brand::after {
  width: 320px;
  height: 320px;
  left: -100px;
  bottom: -120px;
  background: radial-gradient(circle, rgba(147, 197, 253, 0.28), transparent 70%);
}

.brand-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  max-width: 460px;
  margin: 0 auto;
  width: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 44px;
}

.logo-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.logo-text {
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.brand-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.5;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.brand-desc {
  margin: 18px 0 0;
  font-size: 14px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.76);
}

/* 装饰日程卡 */
.schedule {
  margin-top: 44px;
  padding: 16px 18px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.11);
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(6px);
  max-width: 340px;
}

.sc-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.14);
}

.sc-room {
  font-size: 13px;
  font-weight: 500;
}

.sc-tag {
  padding: 1px 8px;
  border-radius: 20px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
}

.sc-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 7px 0;
}

.sc-time {
  width: 38px;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.62);
  font-variant-numeric: tabular-nums;
}

.sc-bar {
  flex: 1;
  height: 7px;
  border-radius: 4px;
}

.sc-bar.busy {
  background: rgba(255, 255, 255, 0.55);
}

.sc-bar.free {
  border: 1px dashed rgba(255, 255, 255, 0.42);
}

.sc-state {
  width: 42px;
  text-align: right;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

.brand-foot {
  position: relative;
  z-index: 1;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
}

/* ============ 右侧表单区 ============ */
.form-side {
  flex: 0 0 46%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 32px;
  background: #fff;
}

.form-box {
  width: 100%;
  max-width: 358px;
}

.form-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 600;
  color: #1f2d3d;
  letter-spacing: 0.5px;
}

.form-sub {
  margin: 0 0 30px;
  font-size: 13.5px;
  color: #8a94a6;
}

.form-box :deep(.el-form-item) {
  margin-bottom: 20px;
}

.form-box :deep(.el-input__wrapper) {
  min-height: 46px;
  padding: 2px 14px;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e3e8ef inset;
  transition: box-shadow 0.2s;
}

.form-box :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c9d4e3 inset;
}

.form-box :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2563eb inset, 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-box :deep(.el-input__inner) {
  font-size: 14.5px;
}

.submit {
  width: 100%;
  height: 46px;
  margin-top: 4px;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #2563eb, #1d4ed8);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.26);
}

.submit:hover {
  background: linear-gradient(90deg, #2f6ef0, #2255e8);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.32);
}

.footer {
  margin-top: 20px;
  text-align: center;
  font-size: 13.5px;
  color: #8a94a6;
}

/* ============ 响应式 ============ */
@media (max-width: 900px) {
  .brand {
    display: none;
  }

  .form-side {
    flex: 1;
    padding: 32px 24px;
  }
}
</style>
