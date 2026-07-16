// 前端校验常量与规则生成器
// 规则需与后端 ValidationConstants 保持一致

export const PHONE_REGEX = /^1[3-9]\d{9}$/
export const PASSWORD_MIN = 6
export const PASSWORD_MAX = 20

export const phoneRule = {
  required: true,
  pattern: PHONE_REGEX,
  message: '手机号格式不正确',
  trigger: 'blur'
}

export const passwordRule = {
  required: true,
  min: PASSWORD_MIN,
  max: PASSWORD_MAX,
  message: '密码长度必须在6-20位之间',
  trigger: 'blur'
}
