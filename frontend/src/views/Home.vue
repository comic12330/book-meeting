<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span class="page-title">系统公告</span>
          </template>
          <el-empty v-if="notices.length === 0" description="暂无公告" />
          <div v-for="item in notices" :key="item.id" class="notice">
            <div class="notice-head">
              <el-icon color="#409eff"><Bell /></el-icon>
              <span class="notice-title">{{ item.title }}</span>
              <span class="notice-time">{{ item.publishTime }}</span>
            </div>
            <div class="notice-body">{{ item.content }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="page-title">快速入口</span>
          </template>
          <el-button type="primary" class="quick" @click="$router.push('/rooms')">
            去预定会议室
          </el-button>
          <el-button class="quick" @click="$router.push('/my-books')">
            查看我的预定
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { getNotices } from '../api'

const notices = ref([])

onMounted(async () => {
  const res = await getNotices()
  notices.value = res.data || []
})
</script>

<style scoped>
.notice {
  padding: 12px 0;
  border-bottom: 1px dashed #ebeef5;
}

.notice:last-child {
  border-bottom: none;
}

.notice-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-title {
  font-size: 14px;
  font-weight: 500;
}

.notice-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.notice-body {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
}

.quick {
  width: 100%;
  margin: 0 0 10px;
}

.quick:last-child {
  margin-bottom: 0;
}
</style>
