import request from './request'

export const userApi = {
  login: (code) => request.post('/user/login', { code }),
  info: () => request.get('/user/info')
}
