<template>
  <div class="login-wrap">
    <el-card class="login-card" shadow="always">
      <h2 class="title">会议室预定系统</h2>
      <p class="sub">基于 Spring Boot + Vue 的会议室资源管理平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password
                    :prefix-icon="Lock" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>
      <div class="footer">
        <span>还没有账号？</span>
        <el-button link type="primary" @click="$router.push('/register')">立即注册</el-button>
      </div>
      <el-alert class="tip" type="info" :closable="false"
                title="默认账号：admin / 123456（管理员），zhangsan / 123456（普通用户）" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api'
import { setAuth } from '../utils/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })

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

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2f7;
}

.login-card {
  width: 400px;
  padding: 8px 12px 16px;
}

.title {
  margin: 8px 0 4px;
  font-size: 20px;
  font-weight: 500;
  text-align: center;
}

.sub {
  margin: 0 0 24px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.submit {
  width: 100%;
}

.footer {
  margin-top: 12px;
  text-align: center;
  font-size: 13px;
  color: #606266;
}

.tip {
  margin-top: 14px;
}
</style>
