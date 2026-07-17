import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import Layout from '../components/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'venue',
        name: 'Venue',
        component: () => import('../views/venue/VenueList.vue'),
        meta: { title: '场地管理' }
      },
      {
        path: 'venue-slot',
        name: 'VenueSlot',
        component: () => import('../views/venue/VenueSlot.vue'),
        meta: { title: '场地时段' }
      },
      {
        path: 'coach',
        name: 'Coach',
        component: () => import('../views/coach/CoachList.vue'),
        meta: { title: '教练管理' }
      },
      {
        path: 'coach-course',
        name: 'CoachCourse',
        component: () => import('../views/coach/CourseList.vue'),
        meta: { title: '课程管理' }
      },
      {
        path: 'coach-schedule',
        name: 'CoachSchedule',
        component: () => import('../views/coach/ScheduleList.vue'),
        meta: { title: '排班管理' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/shop/CategoryList.vue'),
        meta: { title: '商品分类' }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('../views/shop/ProductList.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('../views/shop/OrderList.vue'),
        meta: { title: '订单管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  // 未登录且访问非公开页，跳转登录
  if (!to.meta.public && !userStore.token) {
    next('/login')
    return
  }
  // 已登录但非管理员角色访问后台，清空登录态并跳转登录
  if (!to.meta.public && userStore.userInfo?.role !== 'ADMIN') {
    userStore.logout()
    next('/login')
    return
  }
  next()
})

export default router
