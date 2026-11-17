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

export const adminGetNotices = () => request.get('/admin/notices')
export const adminSaveNotice = (data) => request.post('/admin/notices', data)
export const adminUpdateNotice = (id, data) => request.put(`/admin/notices/${id}`, data)
export const adminDeleteNotice = (id) => request.delete(`/admin/notices/${id}`)
