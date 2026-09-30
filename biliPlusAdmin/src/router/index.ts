import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
    {
        path: '/Home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        redirect: '/Home/Dashboard',
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
            },
            {
                path: 'WalletTx',
                name: 'WalletTx',
                component: () => import('@/views/Wallet/WalletTx.vue'),
                meta: { title: '钱包账变' }
            },
            {
                path: 'Report',
                name: 'ReportManage',
                component: () => import('@/views/Report/ReportManage.vue'),
                meta: { title: '举报处理' }
            },
            {
                path: 'Dashboard',
                name: 'Dashboard',
                component: () => import('@/views/Dashboard/Dashboard.vue'),
                meta: { title: '数据看板' }
            },
            {
                path: 'OperationLog',
                name: 'OperationLog',
                component: () => import('@/views/OperationLog/OperationLog.vue'),
                meta: { title: '操作日志' }
            },
            {
                path: 'SensitiveWord',
                name: 'SensitiveWordManage',
                component: () => import('@/views/SensitiveWord/SensitiveWordManage.vue'),
                meta: { title: '敏感词治理' }
            },
            {
                path: 'User',
                name: 'UserManage',
                component: () => import('@/views/User/UserManage.vue'),
                meta: { title: '用户管理' }
            },
            {
                path: 'Governance',
                name: 'GovernanceDashboard',
                component: () => import('@/views/Governance/GovernanceDashboard.vue'),
                meta: { title: '治理报表' }
            },
            {
                path: 'Notification',
                name: 'NotificationSend',
                component: () => import('@/views/Notification/NotificationSend.vue'),
                meta: { title: '系统通知' }
            }
        ]
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

router.beforeEach((to, _from, next) => {
    const token = localStorage.getItem('token')
    const requiresAuth = to.path.startsWith('/Home')

    if (requiresAuth && !token) {
        next('/')
        return
    }
    next()
})

export default router
