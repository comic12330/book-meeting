/**
 * 时间展示工具。
 *
 * 后端统一以 yyyy-MM-dd HH:mm:ss 返回时间（见 JacksonConfig 的序列化格式），
 * 但预定业务的时间精度只到分钟——没人会把会议约到 10:16:42 结束。
 * 所以列表展示时把秒截掉。
 *
 * 用字符串截取而不是 new Date() 解析：后端给的就是定宽格式，
 * 截取没有时区风险，也不会因为浏览器差异产生偏移。
 */
export function minuteText(value) {
  if (!value) {
    return ''
  }
  const text = String(value)
  return text.length >= 16 ? text.slice(0, 16) : text
}
