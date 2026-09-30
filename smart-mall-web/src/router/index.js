import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/Login.vue')
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('@/views/Register.vue')
    },
    {
        path: '/chat',
        name: 'chat',
        component: () => import('@/views/Chat.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/admin',
        component: () => import('@/views/admin/AdminLayout.vue'),
        meta: { requiresAdmin: true },
        redirect: '/admin/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'admin-dashboard',
                component: () => import('@/views/admin/Dashboard.vue'),
                meta: { title: '数据看板' }
            },
            {
                path: 'user',
                name: 'admin-user',
                component: () => import('@/views/admin/UserManage.vue'),
                meta: { title: '用户管理' }
            },
            {
                path: 'product',
                name: 'admin-product',
                component: () => import('@/views/admin/ProductManage.vue'),
                meta: { title: '商品管理' }
            },
            {
                path: 'order',
                name: 'admin-order',
                component: () => import('@/views/admin/OrderManage.vue'),
                meta: { title: '订单管理' }
            },
            {
                path: 'session',
                name: 'admin-session',
                component: () => import('@/views/admin/SessionManage.vue'),
                meta: { title: '会话记录' }
            }
        ]
    },
    { path: '/', redirect: '/login' },
    { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

/**
 * 路由守卫：本项目后端不校验身份，这里是唯一的"门"
 * 规则就三条：
 *   1. requiresAdmin 的页面：必须已登录 且 role === 1，否则踢回登录页
 *   2. requiresAuth  的页面：必须已登录
 *   3. 其余放行
 */
router.beforeEach((to) => {
    const store = useUserStore()

    if (to.meta.requiresAdmin) {
        return (store.isLoggedIn && store.user.role === 1) ? true : { name: 'login' }
    }
    if (to.meta.requiresAuth && !store.isLoggedIn) {
        return { name: 'login' }
    }
    return true
})

export default router
