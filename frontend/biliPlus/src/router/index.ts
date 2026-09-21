import {createRouter, createWebHistory} from "vue-router";
import type {RouteRecordRaw} from "vue-router";
import { ElMessage } from "element-plus";

// 路由配置 - 全部使用懒加载

const routers: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: {title: '首页'}
    },
    {
        path: '/channel/:type',
        name: 'Channel',
        component: () => import('@/views/channel/ChannelPage.vue'),
        meta: {title: '频道'}
    },
    {
        path: '/anime',
        name: 'Anime',
        component: () => import('@/views/anime/AnimeSquare.vue'),
        meta: {title: '番剧'}
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/UserLogin.vue'),
        meta: {title: '登录'}
    },
    // 注册不再使用独立页面：views/login/Register.vue 本就是空壳，
    // 实际注册流程在 Header 的注册弹窗里，登录页链接用 /?register=1 触发
    {
        path: '/contribute',
        name: 'Contribute',
        component: () => import('@/views/contribution/ContributionPage.vue'),
        meta: {
            title: '投稿',
            requiresAuth: true
        }
    },
    {
        path: '/video/:id',
        name: 'VideoDetail',
        component: () => import('@/views/home/Main/Video/VideoDetail.vue'),
        meta: {title: '视频播放'}
    },
    {
        path: '/setting',
        name: 'Setting',
        component: () => import('@/views/setting/SettingHome.vue'),
        redirect: '/setting/profile',
        meta: {requiresAuth: true, title: '设置'},
        children: [
            {
                path: 'profile',
                name: 'profile',
                component: () => import('@/views/setting/components/ProfileSettings.vue'),
                meta: { title: '个人资料设置' }
            },
            {
                path: 'videoSet',
                name: 'videoSet',
                component: () => import('@/views/setting/components/VideoSettings.vue'),
                meta: { title: '稿件设置' }
            },
            {
                path: 'security',
                name: 'security',
                component: () => import('@/views/setting/components/SecuritySettings.vue'),
                meta: { title: '账号安全' }
            },
            {
                path: 'player',
                name: 'player',
                component: () => import('@/views/setting/components/PlayerSettings.vue'),
                meta: { title: '播放设置' }
            },
            {
                path: 'quality',
                name: 'quality',
                component: () => import('@/views/setting/components/QualitySettings.vue'),
                meta: { title: '画质与缓存' }
            },
            {
                path: 'notification',
                name: 'notification',
                component: () => import('@/views/setting/components/NotificationSettings.vue'),
                meta: { title: '通知设置' }
            },
            {
                path: 'privacy',
                name: 'privacy',
                component: () => import('@/views/setting/components/PrivacySettings.vue'),
                meta: { title: '隐私设置' }
            },
            {
                path: 'shortcut',
                name: 'shortcut',
                component: () => import('@/views/setting/components/ShortcutSettings.vue'),
                meta: { title: '快捷键设置' }
            }
        ]
    },
    {
        path: '/search',
        name: 'Search',
        component: () => import('@/views/search/SearchPage.vue'),
        meta: {title: '搜索'}
    },
    {
        path: '/user/:id',
        name: 'UserSpace',
        component: () => import('@/views/user/UserSpace.vue'),
        meta: {title: '个人主页'}
    },
    {
        // 当前登录用户个人主页快捷入口
        path: '/me',
        name: 'MySpace',
        redirect: () => {
            try {
                const stored = localStorage.getItem('userInfo')
                const info = stored ? JSON.parse(stored) : null
                if (info?.id) {
                    return { name: 'UserSpace', params: { id: info.id } }
                }
            } catch {
                // ignore
            }
            return '/login'
        }
    },
    {
        path: '/chat',
        name: 'ChatView',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: {requiresAuth: true, title: '消息'}
    },
    {
        path: '/live',
        name: 'LiveSquare',
        component: () => import('@/views/live/LiveSquare.vue'),
        meta: {title: '直播广场'}
    },
    {
        path: '/live/room/:id',
        name: 'LiveRoom',
        component: () => import('@/views/live/LiveRoom.vue'),
        meta: {title: '直播间'}
    },
    {
        path: '/live/start',
        name: 'LiveStart',
        component: () => import('@/views/live/LiveStart.vue'),
        meta: {requiresAuth: true, title: '开播'}
    },
    {
        path: '/notifications',
        name: 'NotificationCenter',
        component: () => import('@/views/notify/NotificationPage.vue'),
        meta: {requiresAuth: true, title: '通知中心'}
    },
    {
        path: '/dynamic',
        name: 'DynamicFeed',
        component: () => import('@/views/dynamic/DynamicFeed.vue'),
        meta: {title: '动态'}
    },
    {
        // 404
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/404.vue'),
        meta: {title: '页面未找到'}
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes: routers
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    const title = to.meta?.title as string
    document.title = title ? `${title} - NexusPlay` : 'NexusPlay'

    // 检查登录
    if (to.meta?.requiresAuth) {
        const token = localStorage.getItem('token')
        if (!token) {
            ElMessage.warning('请先登录')
            next('/login')
            return
        }
    }

    next()
})

export default router