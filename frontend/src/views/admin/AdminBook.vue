<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
          <el-option label="已取消" :value="3" />
          <el-option label="已完成" :value="4" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="申请人 / 用途" clearable style="width: 180px"
                  @keyup.enter="load(1)" />
        <el-button type="primary" @click="load(1)">查询</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="申请人" width="110">
          <template #default="{ row }">{{ row.realName }}</template>
        </el-table-column>
        <el-table-column prop="roomName" label="会议室" min-width="110" />
        <el-table-column prop="startTime" label="开始时间" width="165" />
        <el-table-column prop="endTime" label="结束时间" width="165" />
        <el-table-column prop="purpose" label="用途" min-width="130" show-overflow-tooltip />
        <el-table-column prop="participantNum" label="人数" width="70" align="center" />
        <el-table-column label="状态" width="95" align="center">
          <template #default="{ row }">
            <el-tag :type="BOOK_STATUS[row.status].type" size="small">
              {{ BOOK_STATUS[row.status].text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditMsg" label="审核意见" min-width="130" show-overflow-tooltip />
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link @click="onAudit(row, 1)">通过</el-button>
              <el-button type="danger" link @click="openReject(row)">驳回</el-button>
            </template>
            <span v-else class="muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :total="total"
                       :page-size="query.pageSize" :current-page="query.pageNum" @current-change="load" />
      </div>
    </el-card>

    <el-dialog v-model="rejectVisible" title="驳回预定" width="460px">
      <el-form label-width="70px">
        <el-form-item label="理由">
          <el-input v-model="rejectMsg" type="textarea" :rows="3" placeholder="驳回时必须填写理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="auditing" @click="onConfirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetBooks, adminAudit } from '../../api'
import { BOOK_STATUS } from '../../utils/dict'

const loading = ref(false)
const auditing = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: 0, keyword: '' })

const rejectVisible = ref(false)
const rejectMsg = ref('')
const currentRow = ref(null)

async function load(page) {
  if (page) {
    query.pageNum = page
  }
  loading.value = true
  try {
    const res = await adminGetBooks(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function onAudit(row, status) {
  await ElMessageBox.confirm(
    `确定为「${row.realName}」的「${row.roomName}」预定通过吗？系统会再次校验时段冲突。`,
    '审核确认',
    { type: 'warning' }
  )
  auditing.value = true
  try {
    await adminAudit(row.id, { status, auditMsg: '审核通过' })
    ElMessage.success('已通过')
    load()
  } finally {
    auditing.value = false
  }
}

function openReject(row) {
  currentRow.value = row
  rejectMsg.value = ''
  rejectVisible.value = true
}

async function onConfirmReject() {
  if (!rejectMsg.value.trim()) {
    ElMessage.warning('驳回时必须填写理由')
    return
  }
  auditing.value = true
  try {
    await adminAudit(currentRow.value.id, { status: 2, auditMsg: rejectMsg.value.trim() })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    load()
  } finally {
    auditing.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.muted {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
