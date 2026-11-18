<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-radio-group v-model="query.status" @change="load(1)">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="0">待审核</el-radio-button>
          <el-radio-button :value="1">已通过</el-radio-button>
          <el-radio-button :value="2">已驳回</el-radio-button>
          <el-radio-button :value="3">已取消</el-radio-button>
          <el-radio-button :value="4">已完成</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="roomName" label="会议室" min-width="120" />
        <el-table-column prop="location" label="位置" min-width="140" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="purpose" label="用途" min-width="140" show-overflow-tooltip />
        <el-table-column prop="participantNum" label="人数" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="BOOK_STATUS[row.status].type" size="small">
              {{ BOOK_STATUS[row.status].text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditMsg" label="审核意见" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 0 || row.status === 1" type="danger" link
                       @click="onCancel(row)">取消</el-button>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :total="total"
                       :page-size="query.pageSize" :current-page="query.pageNum" @current-change="load" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 我的预定列表。
 *
 * 只查本人记录：请求不带 userId，后端按令牌取当前用户，前端改参数也查不到别人的。
 * 「取消」按钮只在状态为待审核（0）或已通过（1）时显示，
 * 但时间是否已经过去前端不判断——已开始的会议由后端拦下并给出提示。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBooks, cancelBook } from '../api'
import { BOOK_STATUS } from '../utils/dict'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: null })

async function load(page) {
  if (page) {
    query.pageNum = page
  }
  loading.value = true
  try {
    const res = await getMyBooks(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function onCancel(row) {
  // 取消是状态变更，先二次确认再发请求，避免误点
  await ElMessageBox.confirm(`确定取消「${row.roomName}」的预定吗？`, '提示', { type: 'warning' })
  // 取消成功后该时段立即释放，可以重新被别人预定
  await cancelBook(row.id)
  ElMessage.success('已取消')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.muted {
  color: #c0c4cc;
}
</style>
