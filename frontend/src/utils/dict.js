export const EQUIPMENTS = {
  projector: '投影仪',
  microphone: '麦克风',
  video: '视频会议终端'
}

export const BOOK_STATUS = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已驳回', type: 'danger' },
  3: { text: '已取消', type: 'info' },
  4: { text: '已完成', type: 'info' }
}

export const ROOM_STATUS = {
  0: { text: '停用', type: 'info' },
  1: { text: '启用', type: 'success' }
}

export const USER_ROLE = {
  0: { text: '普通用户', type: 'info' },
  1: { text: '管理员', type: 'warning' }
}

export const USER_STATUS = {
  0: { text: '已停用', type: 'info' },
  1: { text: '正常', type: 'success' }
}

export function equipmentText(value) {
  if (!value) {
    return '无'
  }
  return value
    .split(',')
    .filter((item) => item)
    .map((item) => EQUIPMENTS[item] || item)
    .join('、')
}

/**
 * 操作日志字典。与后端 OperationType 枚举一一对应，只增不改——
 * 日志表里存的是 code，改名会让历史记录翻译不出来。
 */
export const LOG_MODULES = {
  ROOM: '会议室',
  BOOK: '预定审核',
  USER: '用户管理',
  NOTICE: '公告管理',
  CONFIG: '系统参数'
}

export const LOG_TYPES = [
  { value: 'ROOM_CREATE', label: '新增会议室', module: 'ROOM' },
  { value: 'ROOM_UPDATE', label: '编辑会议室', module: 'ROOM' },
  { value: 'ROOM_DELETE', label: '删除会议室', module: 'ROOM' },
  { value: 'BOOK_AUDIT_APPROVE', label: '审核通过预定', module: 'BOOK' },
  { value: 'BOOK_AUDIT_REJECT', label: '审核驳回预定', module: 'BOOK' },
  { value: 'BOOK_CANCEL_ADMIN', label: '取消他人预定', module: 'BOOK' },
  { value: 'USER_CREATE', label: '新增账号', module: 'USER' },
  { value: 'USER_UPDATE', label: '编辑账号', module: 'USER' },
  { value: 'USER_STATUS', label: '启用或停用账号', module: 'USER' },
  { value: 'USER_PASSWORD_RESET', label: '重置账号密码', module: 'USER' },
  { value: 'NOTICE_CREATE', label: '发布公告', module: 'NOTICE' },
  { value: 'NOTICE_UPDATE', label: '编辑公告', module: 'NOTICE' },
  { value: 'NOTICE_DELETE', label: '删除公告', module: 'NOTICE' },
  { value: 'CONFIG_UPDATE', label: '修改系统参数', module: 'CONFIG' }
]

export const LOG_RESULT = {
  1: { text: '成功', type: 'success' },
  0: { text: '失败', type: 'danger' }
}

export function logTypeText(value) {
  const item = LOG_TYPES.find((type) => type.value === value)
  return item ? item.label : value
}
