// ========== API 请求层 ==========
const BASE = 'http://localhost:8080/api';

function getToken() { return localStorage.getItem('token') || ''; }
function setToken(t) { localStorage.setItem('token', t); }
function setUser(u) { localStorage.setItem('user', JSON.stringify(u)); }
function getUser() {
  try { return JSON.parse(localStorage.getItem('user')) || {}; } catch(e) { return {}; }
}
function isLogin() { return !!getToken(); }

async function request(method, path, data) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json' }
  };
  const token = getToken();
  if (token) opts.headers['Authorization'] = 'Bearer ' + token;
  if (data && method !== 'GET') opts.body = JSON.stringify(data);

  const res = await fetch(BASE + path, opts);
  const json = await res.json();
  if (json.code === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    if (!path.includes('/user/login')) showLogin();
    throw new Error('请先登录');
  }
  if (json.code !== 200) throw new Error(json.message || '请求失败');
  return json.data;
}

const get = (url) => request('GET', url);
const post = (url, data) => request('POST', url, data);
const put = (url, data) => request('PUT', url, data);
const del = (url) => request('DELETE', url);

// ========== API 接口 ==========
const API = {
  login: (code) => post('/user/login', { code }),
  userInfo: () => get('/user/info'),

  // 场地
  venueList: () => get('/venue/list'),
  venueSlots: (venueId, date) => get(`/venue/slots?venueId=${venueId}&date=${date}`),
  venueAllSlots: (date) => get(`/venue/slots?date=${date}`),
  prepareBooking: (slotId) => post('/venue/booking/prepare', { slotId }),
  cancelVenueBooking: (id) => post(`/venue/booking/cancel/${id}`),

  // 教练
  coachList: () => get('/coach/list'),
  coachDetail: (id) => get(`/coach/detail/${id}`),
  coachCourses: (coachId) => get(`/coach/${coachId}/courses`),
  coachSchedules: (coachId, date, onlyAvailable) => get(`/coach/schedule?coachId=${coachId}&date=${date}&onlyAvailable=${onlyAvailable}`),
  prepareCoachBooking: (scheduleId, courseId) => post('/coach/book', { scheduleId, courseId }),
  cancelCoachBooking: (id) => post(`/coach/book/cancel/${id}`),
  myCoachBookings: (status) => get('/coach/book/my' + (status != null ? `?status=${status}` : '')),

  // 商城
  categories: () => get('/shop/categories'),
  products: (params) => {
    const qs = new URLSearchParams();
    if (params.categoryId) qs.set('categoryId', params.categoryId);
    if (params.keyword) qs.set('keyword', params.keyword);
    return get('/shop/products?' + qs.toString());
  },
  productDetail: (id) => get(`/shop/detail/${id}`),

  // 购物车
  cartList: () => get('/shop/cart/list'),
  addCart: (skuId, quantity) => post('/shop/cart/add', { skuId, quantity }),
  updateCartQty: (id, quantity) => put(`/shop/cart/${id}?quantity=${quantity}`),
  deleteCart: (id) => del(`/shop/cart/${id}`),
  selectCart: (id, selected) => post(`/shop/cart/select/${id}?selected=${selected}`),

  // 订单
  createOrder: (data) => post('/shop/order/create', data),
  cancelOrder: (id) => post(`/shop/order/cancel/${id}`),
  receiveOrder: (id) => post(`/shop/order/receive/${id}`),
  myOrders: (status) => {
    const qs = status != null ? `?status=${status}` : '';
    return get('/shop/order/my' + qs);
  },
  orderDetail: (id) => get(`/shop/order/${id}`)
};
