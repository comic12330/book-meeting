<template>
  <div class="auth-page">
    <AuthBrand />

    <main class="auth-side">
      <div class="auth-form-wrap">
        <h2 class="auth-title">注册账号</h2>

        <el-form ref="formRef" :model="form" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号（3-20 位）">
              <template #prefix>
                <svg class="auth-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7.5" r="3.8" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码（6-32 位）" show-password>
              <template #prefix>
                <svg class="auth-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3.5" y="10.5" width="17" height="10" rx="2.4" />
                  <path d="M7.5 10.5V7a4.5 4.5 0 0 1 9 0v3.5" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名">
              <template #prefix>
                <svg class="auth-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="2.5" y="4.5" width="19" height="15" rx="2.5" />
                  <circle cx="8.5" cy="10.5" r="2" />
                  <path d="M5.5 15.8c.6-1.6 1.7-2.4 3-2.4s2.4.8 3 2.4" />
                  <path d="M14.5 10h4M14.5 13.5h4" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号（选填）" @keyup.enter="onSubmit">
              <template #prefix>
                <svg class="auth-icon" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="6" y="2.5" width="12" height="19" rx="2.6" />
                  <path d="M10.8 18.6h2.4" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-button type="primary" class="auth-submit" :loading="loading" @click="onSubmit">
            注 册
          </el-button>
        </el-form>

        <div class="auth-foot">
          <span>已有账号？</span>
          <el-button link type="primary" @click="$router.push('/login')">返回登录</el-button>
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
import { register } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '', realName: '', phone: '' })

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度需在 3-20 位之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6-32 位之间', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>
