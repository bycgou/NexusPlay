import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
    {
        path: '/Home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        redirect: '/Home/VideoShenHe',
        children: [
            {
                path: 'VideoShenHe',
                name: 'VideoShenHe',
                component: () => import('@/views/Video/VideoShenHe.vue'),
                meta: { title: '视频审核' }
            },
            {
                path: 'VideoShenHe/Detail/:id',
                name: 'VideoShenHeDetail',
                component: () => import('@/views/Video/VideoShenHeDetails.vue'),
                meta: { title: '审核详情' }
            },
            {
                path: 'Category',
                name: 'CategoryManage',
                component: () => import('@/views/Category/CategoryManage.vue'),
                meta: { title: '分类管理' }
            },
            {
                path: 'Banner',
                name: 'BannerManage',
                component: () => import('@/views/Banner/BannerManage.vue'),
                meta: { title: '轮播图管理' }
            }
        ]
    },
    {
        path: '/about',
        name: 'About',
        component: () => import('@/views/About.vue')
    },
    {
        path: '/',
        name: 'Login',
        component: () => import('@/views/Login.vue')
    },
    {
        path: '/:pathMatch(.*)*',
        name: '404',
        component: () => import('@/views/404.vue')
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const requiresAuth = to.path.startsWith('/Home')

    if (requiresAuth && !token) {
        next('/')
        return
    }
    next()
})

export default router
