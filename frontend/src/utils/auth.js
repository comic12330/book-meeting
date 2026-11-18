/**
 * 登录态的本地存储：令牌与用户信息都存在 localStorage，刷新页面后不丢。
 *
 * 注意：这里的 isAdmin 只用于前端隐藏菜单、拦路由，属于体验层。
 * 真正的权限判断在后端拦截器里（按 /api/admin/ 前缀校验角色），
 * 改 localStorage 里的 role 只能骗过界面，接口照样返回 403。
 */
const TOKEN_KEY = 'mr_token'
const USER_KEY = 'mr_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export function setAuth(data) {
  localStorage.setItem(TOKEN_KEY, data.token)
  localStorage.setItem(USER_KEY, JSON.stringify(data))
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function isAdmin() {
  const user = getUser()
  return !!user && user.role === 1
}
