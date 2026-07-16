import request from './request'

export const venueApi = {
  list: () => request.get('/venue/list'),
  detail: (id) => request.get(`/venue/detail/${id}`),
  create: (data) => request.post('/venue', data),
  update: (id, data) => request.put(`/venue/${id}`, data),
  delete: (id) => request.delete(`/venue/${id}`),
  generateSlots: (date) => request.post(`/venue/slots/generate?date=${date}`),
  listSlots: (params) => request.get('/venue/slots', { params })
}
