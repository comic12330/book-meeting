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
          <el-input-number v-model="query.minCapacity" :min="1" :max="limit.maxCapacity || undefined"
                           controls-position="right" style="width: 120px" />
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
      <el-alert v-if="limitText" class="rule" type="info" :closable="false" :title="limitText" />
      <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="90px">
        <el-form-item label="会议室">
          <el-input :model-value="currentRoom.roomName" disabled />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="bookForm.startTime" type="datetime" placeholder="选择开始时间"
                          value-format="YYYY-MM-DD HH:mm:ss" :disabled-date="disabledDate"
                          style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="bookForm.endTime" type="datetime" placeholder="选择结束时间（可跨天）"
                          value-format="YYYY-MM-DD HH:mm:ss" :disabled-date="disabledDate"
                          style="width: 100%" />
        </el-form-item>
        <el-form-item label="会议用途" prop="purpose">
          <el-input v-model="bookForm.purpose" type="textarea" :rows="2" placeholder="简要说明会议用途" />
        </el-form-item>
        <el-form-item label="参会人数" prop="participantNum">
          <el-input-number v-model="bookForm.participantNum" :min="1" :max="currentRoom.capacity" />
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
/**
 * 会议室列表与预定提交。
 *
 * 列表只显示启用中的会议室（由后端 pageForUser 强制 status=1，前端不需要再判断）。
 * 本页的校验只承担「让用户少走一趟」的作用：必填、结束晚于开始、人数不超容量。
 * 时段是否与已有预定冲突、开始时间是否已经过去，前端一律不判断——
 * 那属于必须由后端把关的规则，前端算不算都不影响最终结果。
 *
 * 可预定窗口（每日起止时刻、最多提前天数、单次时长上限）从 /api/configs 读取，
 * 用来限制日期选择器与提示文案。这些值由管理员在「系统参数」页面维护，
 * 前端只负责展示同一套口径，真正的判定仍在后端。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRooms, submitBook, getConfigs } from '../api'
import { EQUIPMENTS, equipmentText } from '../utils/dict'

const loading = ref(false)
const rooms = ref([])
const total = ref(0)

// 可预定窗口，全部来自后端配置；取不到时留空，前端不额外限制
const limit = reactive({
  maxCapacity: 0,
  maxHours: 0,
  maxAdvanceDays: 0,
  dailyStart: '',
  dailyEnd: ''
})

const limitText = computed(() => {
  const parts = []
  if (limit.dailyStart && limit.dailyEnd) {
    parts.push(`每日 ${limit.dailyStart}–${limit.dailyEnd} 之间开始`)
  }
  if (limit.maxAdvanceDays > 0) {
    parts.push(`最多提前 ${limit.maxAdvanceDays} 天`)
  }
  if (limit.maxHours > 0) {
    parts.push(`单次不超过 ${limit.maxHours} 小时`)
  }
  parts.push('结束时间可跨到次日')
  return parts.join('，')
})

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

/** 读取可预定窗口；失败时不阻断页面，仅不显示限制提示，最终由后端校验 */
async function loadLimit() {
  try {
    const res = await getConfigs()
    const data = res.data || {}
    limit.maxCapacity = Number(data['room.max-capacity'] || 0)
    limit.maxHours = Number(data['booking.max-hours'] || 0)
    limit.maxAdvanceDays = Number(data['booking.max-advance-days'] || 0)
    limit.dailyStart = data['booking.daily-start'] || ''
    limit.dailyEnd = data['booking.daily-end'] || ''
  } catch (e) {
    // 忽略：配置读不到只影响前端提示，不影响提交
  }
}

/** 日期选择器禁用：早于今天、超出最多提前天数的日期 */
function disabledDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (date.getTime() < today.getTime()) {
    return true
  }
  if (limit.maxAdvanceDays > 0) {
    const last = new Date(today)
    last.setDate(last.getDate() + limit.maxAdvanceDays)
    return date.getTime() > last.getTime()
  }
  return false
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
  // 两个时间都是 "YYYY-MM-DD HH:mm:ss" 格式的字符串，字典序与时间先后一致，
  // 所以可以直接比较字符串，不必转成 Date 对象。
  // 这里只拦住「结束不晚于开始」这一种，跨天场景是合法的（如 23:00 到次日 02:00）。
  if (bookForm.endTime <= bookForm.startTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  submitting.value = true
  try {
    // 提交后不在这里判断冲突：后端校验失败时会返回具体时段，由 axios 拦截器统一弹出提示
    await submitBook(bookForm)
    ElMessage.success('预定申请已提交，等待管理员审核')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  load()
  loadLimit()
})
</script>

<style scoped>
.mt {
  margin-top: 16px;
}

.rule {
  margin-bottom: 14px;
}

.capacity-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
