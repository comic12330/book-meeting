<template>
  <div class="page">
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="名称">
          <el-input v-model="query.roomName" placeholder="会议室名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="query.location" placeholder="如 行政楼" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="最少容纳">
          <el-input-number v-model="query.minCapacity" :min="1" :max="500" controls-position="right"
                           style="width: 120px" />
        </el-form-item>
        <el-form-item label="设备">
          <el-select v-model="query.equipment" placeholder="不限" clearable style="width: 150px">
            <el-option v-for="(label, code) in EQUIPMENTS" :key="code" :label="label" :value="code" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load(1)">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mt">
      <el-table :data="rooms" border stripe v-loading="loading">
        <el-table-column prop="roomName" label="会议室" min-width="120" />
        <el-table-column prop="location" label="位置" min-width="140" />
        <el-table-column prop="capacity" label="容纳人数" width="100" align="center" />
        <el-table-column label="可用设备" min-width="180">
          <template #default="{ row }">{{ equipmentText(row.equipment) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openBook(row)">预定</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :total="total"
                       :page-size="query.pageSize" :current-page="query.pageNum"
                       @current-change="load" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="提交预定申请" width="520px">
      <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="90px">
        <el-form-item label="会议室">
          <el-input :model-value="currentRoom.roomName" disabled />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="bookForm.startTime" type="datetime" placeholder="选择开始时间"
                          value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="bookForm.endTime" type="datetime" placeholder="选择结束时间（可跨天）"
                          value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="会议用途" prop="purpose">
          <el-input v-model="bookForm.purpose" type="textarea" :rows="2" placeholder="简要说明会议用途" />
        </el-form-item>
        <el-form-item label="参会人数" prop="participantNum">
          <el-input-number v-model="bookForm.participantNum" :min="1" :max="currentRoom.capacity || 500" />
          <span class="capacity-tip">该会议室容纳上限 {{ currentRoom.capacity }} 人</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRooms, submitBook } from '../api'
import { EQUIPMENTS, equipmentText } from '../utils/dict'

const loading = ref(false)
const rooms = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  roomName: '',
  location: '',
  minCapacity: undefined,
  equipment: ''
})

const dialogVisible = ref(false)
const submitting = ref(false)
const bookFormRef = ref(null)
const currentRoom = ref({})
const bookForm = reactive({
  roomId: null,
  startTime: '',
  endTime: '',
  purpose: '',
  participantNum: 1
})

const bookRules = {
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  purpose: [{ required: true, message: '请填写会议用途', trigger: 'blur' }],
  participantNum: [{ required: true, message: '请填写参会人数', trigger: 'change' }]
}

async function load(page) {
  if (page) {
    query.pageNum = page
  }
  loading.value = true
  try {
    const res = await getRooms(query)
    rooms.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.roomName = ''
  query.location = ''
  query.minCapacity = undefined
  query.equipment = ''
  load(1)
}

function openBook(room) {
  currentRoom.value = room
  bookForm.roomId = room.id
  bookForm.startTime = ''
  bookForm.endTime = ''
  bookForm.purpose = ''
  bookForm.participantNum = 1
  dialogVisible.value = true
}

async function onSubmit() {
  await bookFormRef.value.validate()
  if (bookForm.endTime <= bookForm.startTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  submitting.value = true
  try {
    await submitBook(bookForm)
    ElMessage.success('预定申请已提交，等待管理员审核')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.mt {
  margin-top: 16px;
}

.capacity-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
