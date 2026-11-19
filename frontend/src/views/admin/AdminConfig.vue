<template>
  <div class="page">
    <el-card shadow="never">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="configName" label="参数名称" min-width="220" />
        <el-table-column label="当前值" width="160" align="center">
          <template #default="{ row }">
            <strong>{{ row.configValue }}</strong>
            <span v-if="row.unit" class="unit">{{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="`修改参数：${form.configName}`" width="480px">
      <el-form label-width="90px">
        <el-form-item label="参数值">
          <el-input-number v-if="form.valueType === 'INT'" v-model="numberValue"
                           :min="form.minValue" :max="form.maxValue" controls-position="right" />
          <el-time-picker v-else v-model="timeValue" format="HH:mm" value-format="HH:mm"
                          placeholder="HH:mm" style="width: 160px" />
          <span v-if="form.unit" class="unit">{{ form.unit }}</span>
        </el-form-item>
        <el-form-item label="取值范围">
          <span class="range">
            {{ form.valueType === 'INT'
              ? `${form.minValue} ~ ${form.maxValue}${form.unit ? ' ' + form.unit : ''}`
              : '起始时间须早于最晚开始时间' }}
          </span>
        </el-form-item>
        <el-form-item label="说明">
          <span class="range">{{ form.remark }}</span>
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
 * 系统业务参数维护。
 *
 * 参数项由数据库初始化脚本写入，页面只允许改值——键名是代码里声明过的契约
 * （后端 SysConfigKey 枚举），在页面上增删键会导致代码读不到对应配置。
 * 保存后后端直接刷新内存缓存，业务校验立刻按新值执行。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminGetConfigs, adminSaveConfig } from '../../api'

const loading = ref(false)
const saving = ref(false)
const list = ref([])

const dialogVisible = ref(false)
const form = reactive({
  configKey: '',
  configName: '',
  configValue: '',
  unit: '',
  remark: '',
  valueType: 'INT',
  minValue: undefined,
  maxValue: undefined
})
const numberValue = ref(0)
const timeValue = ref('')

async function load() {
  loading.value = true
  try {
    const res = await adminGetConfigs()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.assign(form, row)
  if (row.valueType === 'TIME') {
    timeValue.value = row.configValue
  } else {
    numberValue.value = Number(row.configValue)
  }
  dialogVisible.value = true
}

async function onSubmit() {
  const value = form.valueType === 'TIME' ? timeValue.value : String(numberValue.value)
  if (!value) {
    ElMessage.warning('参数值不能为空')
    return
  }
  saving.value = true
  try {
    await adminSaveConfig({ configKey: form.configKey, configValue: value })
    ElMessage.success('已保存，即时生效')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.unit {
  margin-left: 6px;
  font-size: 12px;
  color: #909399;
}

.range {
  font-size: 13px;
  color: #606266;
}
</style>
