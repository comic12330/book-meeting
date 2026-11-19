<template>
  <div class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="query.module" placeholder="全部模块" clearable style="width: 140px"
                   @change="onModuleChange">
          <el-option v-for="(label, value) in LOG_MODULES" :key="value" :label="label" :value="value" />
        </el-select>
        <el-select v-model="query.opType" placeholder="全部操作" clearable style="width: 160px">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input v-model="query.operator" placeholder="操作人" clearable style="width: 130px"
                  @keyup.enter="load(1)" />
        <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD"
                        range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
                        style="width: 250px" />
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="70" align="center" :index="indexMethod" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column label="模块" width="100">
          <template #default="{ row }">{{ LOG_MODULES[row.module] || row.module }}</template>
        </el-table-column>
        <el-table-column label="操作内容" min-width="150">
          <template #default="{ row }">{{ logTypeText(row.opType) }}</template>
        </el-table-column>
        <el-table-column label="操作对象" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.targetDesc || '—' }}</template>
        </el-table-column>
        <el-table-column label="结果" width="90" align="center">
          <template #default="{ row }">
            <el-tooltip v-if="row.opResult === 0" :content="row.resultMsg || '操作失败'" placement="top">
              <el-tag :type="LOG_RESULT[0].type" size="small">{{ LOG_RESULT[0].text }}</el-tag>
            </el-tooltip>
            <el-tag v-else :type="LOG_RESULT[1].type" size="small">{{ LOG_RESULT[1].text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="opTime" label="操作时间" width="165" />
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
 * 管理端操作日志。
 *
 * 只读页面：后端没有提供修改与删除接口，这里也就没有对应按钮。
 * 记录范围是管理端的数据变更动作（增删改），普通用户的预定、取消不在其中——
 * 本功能用于追查「谁改了全站数据」，不是做用户行为分析。
 *
 * 失败记录同样入库，且比成功记录更值得看：管理员反复尝试删除有预定的会议室、
 * 给最后一名管理员降级被拦下，这些痕迹都靠失败记录留下来。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { adminGetLogs } from '../../api'
import { LOG_MODULES, LOG_TYPES, LOG_RESULT, logTypeText } from '../../utils/dict'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const range = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, module: '', opType: '', operator: '' })

// 选了模块就把操作类型收窄到该模块，避免出现「模块=公告 + 操作=编辑会议室」这种查不到的条件
const typeOptions = computed(() =>
  query.module ? LOG_TYPES.filter((item) => item.module === query.module) : LOG_TYPES
)

function onModuleChange() {
  query.opType = ''
}

// 序号跨页连续：第 2 页从 11 开始
const indexMethod = (i) => (query.pageNum - 1) * query.pageSize + i + 1

async function load(page) {
  if (page) {
    query.pageNum = page
  }
  loading.value = true
  try {
    // 日期选择器只给到「天」，补齐时分秒才能覆盖整天，后端用左闭右闭区间比较
    const params = { ...query }
    if (range.value && range.value.length === 2) {
      params.startTime = `${range.value[0]} 00:00:00`
      params.endTime = `${range.value[1]} 23:59:59`
    }
    const res = await adminGetLogs(params)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.module = ''
  query.opType = ''
  query.operator = ''
  range.value = []
  load(1)
}

onMounted(() => load())
</script>
