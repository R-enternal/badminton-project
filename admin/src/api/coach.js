import request from './request'

export const coachApi = {
  // 教练
  list: () => request.get('/coach/list'),
  detail: (id) => request.get(`/coach/detail/${id}`),
  create: (data) => request.post('/coach', data),
  update: (id, data) => request.put(`/coach/${id}`, data),
  delete: (id) => request.delete(`/coach/${id}`),
  // 课程
  listCourses: (coachId) => request.get(`/coach/${coachId}/courses`),
  createCourse: (data) => request.post('/coach/course', data),
  updateCourse: (id, data) => request.put(`/coach/course/${id}`, data),
  deleteCourse: (id) => request.delete(`/coach/course/${id}`),
  // 排班
  listSchedule: (params) => request.get('/coach/schedule', { params }),
  createSchedule: (data) => request.post('/coach/schedule', data),
  updateSchedule: (id, data) => request.put(`/coach/schedule/${id}`, data),
  deleteSchedule: (id) => request.delete(`/coach/schedule/${id}`)
}
