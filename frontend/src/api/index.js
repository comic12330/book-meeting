/**
 * 接口封装：按后端路径分组，一个函数对应一个接口。
 *
 * 分成 auth / user（个人中心）/ rooms / notices（用户端）
 * 与 admin*（管理端）两组，管理端函数统一以 admin 开头，
 * 便于在页面里一眼看出这个调用是否要求管理员身份。
 */
import request from './request'

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getProfile = () => request.get('/auth/me')
export const updateProfile = (data) => request.put('/user/profile', data)
export const changePassword = (data) => request.put('/user/password', data)

export const getRooms = (params) => request.get('/rooms', { params })
export const getNotices = () => request.get('/notices')

export const submitBook = (data) => request.post('/books', data)
export const getMyBooks = (params) => request.get('/books/my', { params })
export const cancelBook = (id) => request.put(`/books/${id}/cancel`)

export const adminGetRooms = (params) => request.get('/admin/rooms', { params })
export const adminSaveRoom = (data) => request.post('/admin/rooms', data)
export const adminUpdateRoom = (id, data) => request.put(`/admin/rooms/${id}`, data)
export const adminDeleteRoom = (id) => request.delete(`/admin/rooms/${id}`)

export const adminGetBooks = (params) => request.get('/admin/books', { params })
export const adminAudit = (id, data) => request.put(`/admin/books/${id}/audit`, data)
export const adminCancelBook = (id, data) => request.put(`/admin/books/${id}/cancel`, data)

export const adminGetNotices = () => request.get('/admin/notices')
export const adminSaveNotice = (data) => request.post('/admin/notices', data)
export const adminUpdateNotice = (id, data) => request.put(`/admin/notices/${id}`, data)
export const adminDeleteNotice = (id) => request.delete(`/admin/notices/${id}`)

export const adminGetUsers = (params) => request.get('/admin/users', { params })
export const adminSaveUser = (data) => request.post('/admin/users', data)
export const adminUpdateUser = (id, data) => request.put(`/admin/users/${id}`, data)
export const adminUpdateUserStatus = (id, status) => request.put(`/admin/users/${id}/status/${status}`)
export const adminResetPassword = (id, data) => request.put(`/admin/users/${id}/password`, data)

export const getConfigs = () => request.get('/configs')
export const adminGetConfigs = () => request.get('/admin/configs')
export const adminSaveConfig = (data) => request.put('/admin/configs', data)

// 操作日志为只读：后端没有提供修改与删除接口
export const adminGetLogs = (params) => request.get('/admin/logs', { params })
