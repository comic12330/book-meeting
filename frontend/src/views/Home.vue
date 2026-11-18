<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span class="page-title">系统公告</span>
          </template>
          <el-empty v-if="notices.length === 0" description="暂无公告" />
          <div v-for="item in notices" :key="item.id" class="notice" @click="openDetail(item)">
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

    <el-dialog v-model="detailVisible" :title="current.title" width="600px">
      <div class="detail-time">发布于 {{ current.publishTime }}</div>
      <div class="detail-content">{{ current.content }}</div>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 系统首页：公告列表 + 快速入口。
 *
 * 公告只显示两行摘要（样式里用 -webkit-line-clamp: 2 截断），点击弹窗看全文，
 * 避免一条上千字的公告把首页撑得很长。
 * 数据源接口本身只返回 is_show = 1 的记录，隐藏的公告不会传到这里。
 */
import { ref, onMounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { getNotices } from '../api'

const notices = ref([])
const detailVisible = ref(false)
const current = ref({ title: '', content: '', publishTime: '' })

// 列表与弹窗共用同一条数据，只是展示范围不同，不需要再请求一次接口
function openDetail(item) {
  current.value = item
  detailVisible.value = true
}

onMounted(async () => {
  const res = await getNotices()
  notices.value = res.data || []
})
</script>

<style scoped>
.notice {
  padding: 12px 10px;
  margin: 0 -10px;
  border-bottom: 1px dashed #ebeef5;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
}

.notice:hover {
  background: #f5f8ff;
}

.notice:last-child {
  border-bottom: none;
}

.notice-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice:hover .notice-title {
  color: #409eff;
}

.notice-title {
  font-size: 14px;
  font-weight: 500;
  transition: color 0.15s;
}

.notice-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

/* 列表只显示两行，超出用省略号截断，点击看全文 */
.notice-body {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.detail-time {
  padding-bottom: 12px;
  margin-bottom: 14px;
  border-bottom: 1px solid #ebeef5;
  font-size: 12.5px;
  color: #909399;
}

.detail-content {
  max-height: 52vh;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

.quick {
  width: 100%;
  margin: 0 0 10px;
}

.quick:last-child {
  margin-bottom: 0;
}
</style>
