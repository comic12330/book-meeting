<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="query.username" placeholder="登录账号" clearable style="width: 150px"
                  @keyup.enter="load(1)" />
        <el-input v-model="query.realName" placeholder="真实姓名" clearable style="width: 150px"
                  @keyup.enter="load(1)" />
        <el-select v-model="query.role" placeholder="全部角色" clearable style="width: 130px">
          <el-option label="普通用户" :value="0" />
          <el-option label="管理员" :value="1" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option label="正常" :value="1" />
          <el-option label="已停用" :value="0" />
        </el-select>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="onReset">重置</el-button>
        <el-button type="success" @click="openEdit(null)">新增用户</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="70" align="center" :index="indexMethod" />
        <el-table-column prop="username" label="登录账号" min-width="130" />
        <el-table-column prop="realName" label="真实姓名" min-width="110" />
        <el-table-column prop="phone" label="联系电话" width="130">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="USER_ROLE[row.role].type" size="small">{{ USER_ROLE[row.role].text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="USER_STATUS[row.status].type" size="small">
              {{ USER_STATUS[row.status].text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="openReset(row)">重置密码</el-button>
            <el-button v-if="row.username !== currentUsername" :type="row.status === 1 ? 'danger' : 'success'"
                       link @click="onToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :total="total"
                       :page-size="query.pageSize" :current-page="query.pageNum" @current-change="load" />
      </div>
    </el-card>

    <!-- 新增 / 编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="form.id ? editRules : addRules" label-width="90px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="3-20 位字母、数字或下划线" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6-32 位" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="选填，11 位手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio :value="0">普通用户</el-radio>
            <el-radio :value="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog v-model="pwdVisible" title="重置密码" width="440px">
      <el-alert class="pwd-tip" type="warning" :closable="false"
                :title="`即将重置「${pwdTarget.realName}（${pwdTarget.username}）」的登录密码，重置后原密码立即失效。`" />
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-32 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onResetPassword">确定重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminGetUsers,
  adminSaveUser,
  adminUpdateUser,
  adminUpdateUserStatus,
  adminResetPassword
} from '../../api'
import { USER_ROLE, USER_STATUS } from '../../utils/dict'
import { getUser } from '../../utils/auth'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, username: '', realName: '', role: undefined, status: undefined })

const currentUsername = computed(() => (getUser() || {}).username)

const dialogVisible = ref(false)
const formRef = ref(null)
const emptyForm = { id: null, username: '', password: '', realName: '', phone: '', role: 0, status: 1 }
const form = reactive({ ...emptyForm })

const pwdVisible = ref(false)
const pwdFormRef = ref(null)
const pwdTarget = reactive({ id: null, username: '', realName: '' })
const pwdForm = reactive({ newPassword: '', confirmPassword: '' })

const addRules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '3-20 位字母、数字或下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置初始密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6-32 位之间', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const editRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const pwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6-32 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 序号跨页连续
const indexMethod = (i) => (query.pageNum - 1) * query.pageSize + i + 1

async function load(page) {
  if (page) {
    query.pageNum = page
  }
  loading.value = true
  try {
    const res = await adminGetUsers(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function onReset() {
  Object.assign(query, { pageNum: 1, username: '', realName: '', role: undefined, status: undefined })
  load()
}

function openEdit(row) {
  Object.assign(form, row ? { ...row, password: '' } : { ...emptyForm })
  dialogVisible.value = true
}

async function onSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await adminUpdateUser(form.id, {
        username: form.username,
        realName: form.realName,
        phone: form.phone,
        role: form.role,
        status: form.status
      })
      ElMessage.success('已更新')
    } else {
      await adminSaveUser({ ...form })
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onToggleStatus(row) {
  const enable = row.status !== 1
  const action = enable ? '启用' : '停用'
  await ElMessageBox.confirm(
    enable
      ? `确定启用「${row.realName}（${row.username}）」吗？启用后该账号可正常登录。`
      : `确定停用「${row.realName}（${row.username}）」吗？停用后该账号无法登录，已登录的会话也会立即失效。`,
    '提示',
    { type: 'warning' }
  )
  await adminUpdateUserStatus(row.id, enable ? 1 : 0)
  ElMessage.success(`账号已${action}`)
  load()
}

function openReset(row) {
  Object.assign(pwdTarget, { id: row.id, username: row.username, realName: row.realName })
  Object.assign(pwdForm, { newPassword: '', confirmPassword: '' })
  pwdVisible.value = true
}

async function onResetPassword() {
  await pwdFormRef.value.validate()
  saving.value = true
  try {
    await adminResetPassword(pwdTarget.id, { newPassword: pwdForm.newPassword })
    ElMessage.success('密码已重置')
    pwdVisible.value = false
  } finally {
    saving.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.pwd-tip {
  margin-bottom: 16px;
}
</style>
