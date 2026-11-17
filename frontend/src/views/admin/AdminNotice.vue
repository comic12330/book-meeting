<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="success" @click="openEdit(null)">发布公告</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column label="首页展示" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isShow === 1 ? 'success' : 'info'" size="small">
              {{ row.isShow === 1 ? '展示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="首页展示">
          <el-switch v-model="form.isShow" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetNotices, adminSaveNotice, adminUpdateNotice, adminDeleteNotice } from '../../api'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, title: '', content: '', isShow: 1 })

async function load() {
  loading.value = true
  try {
    const res = await adminGetNotices()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  if (row) {
    Object.assign(form, { id: row.id, title: row.title, content: row.content, isShow: row.isShow })
  } else {
    Object.assign(form, { id: null, title: '', content: '', isShow: 1 })
  }
  dialogVisible.value = true
}

async function onSubmit() {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('标题与内容不能为空')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await adminUpdateNotice(form.id, form)
      ElMessage.success('已更新')
    } else {
      await adminSaveNotice(form)
      ElMessage.success('已发布')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '提示', { type: 'warning' })
  await adminDeleteNotice(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => load())
</script>
