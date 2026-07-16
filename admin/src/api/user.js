import request from './request'

export const userApi = {
  login: (code) => request.post('/user/login', { code }),
  passwordLogin: (phone, password) => request.post('/user/login/password', { phone, password }),
  register: (data) => request.post('/user/register', data),
  info: () => request.get('/user/info')
}
