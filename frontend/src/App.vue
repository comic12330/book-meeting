<template>
  <router-view />
</template>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: system-ui, -apple-system, 'Microsoft YaHei', sans-serif;
  background: #f5f7fa;
  color: #303133;
}

.page {
  padding: 16px;
}

.page-title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 500;
}

.toolbar {
  margin-bottom: 12px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
