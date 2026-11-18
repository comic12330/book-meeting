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

/* ==================== 登录 / 注册页公共样式 ==================== */
.auth-page {
  display: flex;
  min-height: 100vh;
}

.auth-side {
  flex: 0 0 46%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  background: #fff;
}

.auth-form-wrap {
  width: 100%;
  max-width: 356px;
}

.auth-title {
  margin: 0 0 30px;
  font-size: 26px;
  font-weight: 600;
  letter-spacing: 0.4px;
  color: #101828;
}

.auth-form-wrap .el-form-item {
  margin-bottom: 16px;
}

.auth-form-wrap .el-input__wrapper {
  min-height: 48px;
  padding: 0 14px;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e3e8ef inset;
  transition: box-shadow 0.18s;
}

.auth-form-wrap .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #d0dae8 inset;
}

.auth-form-wrap .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px #2563eb inset, 0 0 0 3px rgba(37, 99, 235, 0.11);
}

.auth-form-wrap .el-input__inner {
  font-size: 14.5px;
}

.auth-form-wrap .el-input__inner::placeholder {
  color: #b0b8c4;
}

.auth-form-wrap .el-form-item__error {
  padding-top: 4px;
  font-size: 12px;
}

/* 输入框左侧图标：聚焦时随边框一起变蓝 */
.auth-icon {
  color: #a3adbb;
  transition: color 0.18s;
}

.auth-form-wrap .el-input__wrapper.is-focus .auth-icon {
  color: #2563eb;
}

.auth-form-wrap .el-input__password {
  color: #a3adbb;
  font-size: 17px;
}

.auth-form-wrap .el-input__password:hover {
  color: #6b7789;
}

.el-button.auth-submit {
  width: 100%;
  height: 48px;
  margin-top: 8px;
  border: 0;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #2563eb, #1d4ed8);
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.26);
  transition: transform 0.16s, box-shadow 0.16s, background 0.16s;
}

.el-button.auth-submit:hover {
  transform: translateY(-1px);
  background: linear-gradient(90deg, #2f6cf0, #2255e8);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.32);
}

.el-button.auth-submit:active {
  transform: translateY(0);
}

.auth-foot {
  margin-top: 22px;
  text-align: center;
  font-size: 13.5px;
  color: #8a94a6;
}

@media (max-width: 960px) {
  .auth-page > .brand {
    display: none;
  }

  .auth-side {
    flex: 1;
    padding: 40px 24px;
  }
}
</style>
