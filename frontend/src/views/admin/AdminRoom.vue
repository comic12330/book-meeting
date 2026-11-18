<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="query.roomName" placeholder="会议室名称" clearable style="width: 160px"
                  @keyup.enter="load(1)" />
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button type="success" @click="openEdit(null)">新增会议室</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="70" align="center" :index="indexMethod" />
        <el-table-column prop="roomName" label="会议室" min-width="120" />
        <el-table-column prop="location" label="位置" min-width="140" />
        <el-table-column prop="capacity" label="容纳人数" width="100" align="center" />
        <el-table-column label="可用设备" min-width="170">
          <template #default="{ row }">{{ equipmentText(row.equipment) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="ROOM_STATUS[row.status].type" size="small">
              {{ ROOM_STATUS[row.status].text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="total, prev, pager, next" :total="total"
                       :page-size="query.pageSize" :current-page="query.pageNum" @current-change="load" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑会议室' : '新增会议室'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="roomName">
          <el-input v-model="form.roomName" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="如 行政楼 3 层 301" />
        </el-form-item>
        <el-form-item label="可用设备">
          <el-checkbox-group v-model="equipmentList">
            <el-checkbox v-for="(label, code) in EQUIPMENTS" :key="code" :value="code">
              {{ label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
/**
 * 管理端会议室维护：分页查询、新增、编辑、删除。
 *
 * 与用户端列表的区别是不按状态过滤，停用的会议室在这里也能看到并改回启用。
 * 删除按钮不做前端拦截，直接交给后端判断是否有历史预定，
 * 因为「有没有预定记录」只有数据库知道，前端算不出来。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetRooms, adminSaveRoom, adminUpdateRoom, adminDeleteRoom } from '../../api'
import { EQUIPMENTS, ROOM_STATUS, equipmentText } from '../../utils/dict'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, roomName: '', status: undefined })

const dialogVisible = ref(false)
const formRef = ref(null)
const equipmentList = ref([])
const form = reactive({ id: null, roomName: '', capacity: 10, location: '', status: 1, remark: '' })

const rules = {
  roomName: [{ required: true, message: '请输入会议室名称', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'change' }],
  location: [{ required: true, message: '请输入所在位置', trigger: 'blur' }]
}

// 序号跨页连续：第 2 页从 11 开始，而不是回到 1
const indexMethod = (i) => (query.pageNum - 1) * query.pageSize + i + 1

async function load(page) {
  if (page) {
    query.pageNum = page
  }
  loading.value = true
  try {
    const res = await adminGetRooms(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  if (row) {
    Object.assign(form, {
      id: row.id,
      roomName: row.roomName,
      capacity: row.capacity,
      location: row.location,
      status: row.status,
      remark: row.remark
    })
    equipmentList.value = row.equipment ? row.equipment.split(',').filter((i) => i) : []
  } else {
    Object.assign(form, { id: null, roomName: '', capacity: 10, location: '', status: 1, remark: '' })
    equipmentList.value = []
  }
  dialogVisible.value = true
}

async function onSubmit() {
  await formRef.value.validate()
  // 设备在表单里是多选框数组，入库前拼成逗号分隔字符串，
  // 与后端条件的 FIND_IN_SET(#{equipment}, equipment) 相对应
  const payload = { ...form, equipment: equipmentList.value.join(',') }
  saving.value = true
  try {
    if (form.id) {
      await adminUpdateRoom(form.id, payload)
      ElMessage.success('已更新')
    } else {
      await adminSaveRoom(payload)
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  // 提示里先说明限制，让管理员知道失败时该改用「停用」而不是反复重试
  await ElMessageBox.confirm(`确定删除「${row.roomName}」吗？已有预定记录的会议室不能删除。`, '提示', {
    type: 'warning'
  })
  await adminDeleteRoom(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => load())
</script>
