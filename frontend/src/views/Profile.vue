<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="page-title">基本资料</span></template>
          <el-form :model="profile" label-width="90px">
            <el-form-item label="账号">
              <el-input :model-value="profile.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="profile.realName" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profile.phone" />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="profile.role === 1 ? 'danger' : 'info'" size="small">
                {{ profile.role === 1 ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="注册时间">
              <span>{{ profile.createTime }}</span>
            </el-form-item>
            <el-button type="primary" :loading="saving" @click="onSave">保存修改</el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="page-title">修改密码</span></template>
          <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-width="90px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwd.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwd.newPassword" type="password" show-password placeholder="6-32 位" />
            </el-form-item>
            <el-button type="primary" :loading="changing" @click="onChangePassword">修改密码</el-button>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * 个人资料：查看并修改姓名、手机号，以及修改密码。
 *
 * 只提交 realName 与 phone 两个字段，与后端 updateProfile 的可改范围一致；
 * 角色、账号这些字段即使传上去也不会被写入。
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, changePassword } from '../api'
import { clearAuth } from '../utils/auth'

const router = useRouter()
const profile = reactive({ username: '', realName: '', phone: '', role: 0, createTime: '' })
const saving = ref(false)
const changing = ref(false)
const pwdRef = ref(null)
const pwd = reactive({ oldPassword: '', newPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6-32 位之间', trigger: 'blur' }
  ]
}

onMounted(async () => {
  const res = await getProfile()
  Object.assign(profile, res.data)
})

async function onSave() {
  saving.value = true
  try {
    await updateProfile({ realName: profile.realName, phone: profile.phone })
    ElMessage.success('资料已更新')
  } finally {
    saving.value = false
  }
}

async function onChangePassword() {
  await pwdRef.value.validate()
  changing.value = true
  try {
    await changePassword(pwd)
    // 后端没有让旧令牌失效的机制（需要 token_version 字段），
    // 所以由前端主动清掉本地登录态并要求重新登录，保证改完密码后旧令牌不再被使用
    ElMessage.success('密码已修改，请重新登录')
    clearAuth()
    router.push('/login')
  } finally {
    changing.value = false
  }
}
</script>
