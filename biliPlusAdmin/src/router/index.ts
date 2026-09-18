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
            },
            {
                path: 'Live',
                name: 'LiveManage',
                component: () => import('@/views/Live/LiveManage.vue'),
                meta: { title: '直播管理' }
            },
            {
                path: 'LiveCategory',
                name: 'LiveCategory',
                component: () => import('@/views/Live/LiveCategory.vue'),
                meta: { title: '直播分区' }
            },
            {
                path: 'Gift',
                name: 'GiftManage',
                component: () => import('@/views/Gift/GiftManage.vue'),
                meta: { title: '礼物管理' }
            },
            {
                path: 'GiftRecord',
                name: 'GiftRecord',
                component: () => import('@/views/Gift/GiftRecord.vue'),
                meta: { title: '打赏流水' }
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
