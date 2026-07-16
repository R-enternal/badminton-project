import request from './request'

export const shopApi = {
  // 分类
  listCategories: () => request.get('/shop/categories'),
  createCategory: (data) => request.post('/shop/categories', data),
  updateCategory: (id, data) => request.put(`/shop/categories/${id}`, data),
  deleteCategory: (id) => request.delete(`/shop/categories/${id}`),
  // 商品
  listProducts: (params) => request.get('/shop/products', { params }),
  createProduct: (data) => request.post('/shop/products', data),
  updateProduct: (id, data) => request.put(`/shop/products/${id}`, data),
  deleteProduct: (id) => request.delete(`/shop/products/${id}`),
  productDetail: (id) => request.get(`/shop/detail/${id}`),
  // SKU
  listSkus: (spuId) => request.get(`/shop/products/${spuId}/skus`),
  createSku: (data) => request.post('/shop/skus', data),
  updateSku: (id, data) => request.put(`/shop/skus/${id}`, data),
  deleteSku: (id) => request.delete(`/shop/skus/${id}`),
  // 订单
  listOrders: (params) => request.get('/shop/orders', { params }),
  shipOrder: (id) => request.post(`/shop/order/ship/${id}`),
  orderDetail: (id) => request.get(`/shop/order/${id}`)
}
