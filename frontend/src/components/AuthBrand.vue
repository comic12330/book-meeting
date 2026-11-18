<template>
  <aside class="brand">
    <div class="brand-top">
      <div class="logo">
        <span class="logo-mark">
          <svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4.5" width="18" height="17" rx="2.5" />
            <path d="M16 2.5v4M8 2.5v4M3 10.5h18" />
          </svg>
        </span>
        <span class="logo-text">会议室预定系统</span>
      </div>

      <h1 class="brand-title">让会议室的使用不再冲突</h1>

      <!-- 装饰：会议室时间槽，静态数据不参与业务 -->
      <div class="room-card">
        <div class="room-head">
          <span class="room-name">会议室</span>
          <span class="status-pill">今日</span>
        </div>
        <div v-for="s in schedule" :key="s.time" class="slot">
          <span class="slot-time">{{ s.time }}</span>
          <span class="slot-bar" :class="s.state"></span>
          <span class="slot-state" :class="s.state">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <div class="brand-foot">© 2026 会议预定系统</div>
  </aside>
</template>

<script setup>
const schedule = [
  { time: '09:00', state: 'busy', label: '已预定' },
  { time: '10:00', state: 'busy', label: '已预定' },
  { time: '11:00', state: 'free', label: '空闲' },
  { time: '14:00', state: 'free', label: '空闲' }
]
</script>

<style scoped>
.brand {
  position: relative;
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 52px 56px 26px;
  color: #fff;
  background: linear-gradient(140deg, #0f2a5e 0%, #1d4ed8 52%, #3b82f6 100%);
  overflow: hidden;
}

/* 两团光斑，避免纯渐变发闷 */
.brand::before {
  content: '';
  position: absolute;
  width: 460px;
  height: 460px;
  right: -150px;
  top: -140px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.2), transparent 68%);
}

.brand::after {
  content: '';
  position: absolute;
  width: 360px;
  height: 360px;
  left: -120px;
  bottom: -150px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(147, 197, 253, 0.26), transparent 70%);
}

.brand-top {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 452px;
  /* auto 四向吸收剩余空间：在左栏内水平+垂直居中 */
  margin: auto;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 48px;
}

.logo-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
}

.logo-text {
  font-size: 16.5px;
  font-weight: 500;
  letter-spacing: 0.6px;
}

.brand-title {
  margin: 0 0 42px;
  font-size: 32px;
  line-height: 1.5;
  font-weight: 600;
  letter-spacing: 0.8px;
}

/* 装饰卡：会议室时间槽 */
.room-card {
  max-width: 366px;
  padding: 18px 20px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.17);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.room-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.13);
}

.room-name {
  font-size: 13.5px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.status-pill {
  padding: 2px 9px;
  border-radius: 20px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.19);
  color: rgba(255, 255, 255, 0.92);
}

.slot {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 9px 0;
}

.slot + .slot {
  border-top: 1px solid rgba(255, 255, 255, 0.07);
}

.slot-time {
  width: 46px;
  flex: none;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.62);
  font-variant-numeric: tabular-nums;
}

.slot-bar {
  flex: 1;
  height: 8px;
  border-radius: 4px;
}

.slot-bar.busy {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.66), rgba(255, 255, 255, 0.34));
}

.slot-bar.free {
  border: 1px dashed rgba(255, 255, 255, 0.5);
}

.slot-state {
  width: 46px;
  flex: none;
  text-align: right;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.58);
}

.slot-state.free {
  color: rgba(190, 230, 255, 0.82);
}

.brand-foot {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 452px;
  margin: 0 auto;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.48);
  letter-spacing: 0.3px;
}
</style>
