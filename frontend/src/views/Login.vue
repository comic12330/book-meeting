<template>
  <div class="auth-page">
    <AuthBrand />

    <main class="auth-side">
      <div class="auth-form-wrap">
        <h2 class="auth-title">欢迎回来</h2>

        <el-form ref="formRef" :model="form" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号">
              <template #prefix>
                <svg class="auth-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
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
                <svg class="auth-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3.5" y="10.5" width="17" height="10" rx="2.4" />
                  <path d="M7.5 10.5V7a4.5 4.5 0 0 1 9 0v3.5" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-button type="primary" class="auth-submit" :loading="loading" @click="onSubmit">
            登 录
          </el-button>
        </el-form>

        <div class="auth-foot">
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
import AuthBrand from '../components/AuthBrand.vue'
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
