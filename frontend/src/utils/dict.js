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
