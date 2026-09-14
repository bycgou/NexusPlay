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
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/UserLogin.vue'),
        meta: {title: '登录'}
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/login/Register.vue'),
        meta: {title: '注册'}
    },
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
                component: () => import('@/views/setting/components/ProfileSettings.vue')
            },
            {
                path: 'videoSet',
                name: 'videoSet',
                component: () => import('@/views/setting/components/VideoSettings.vue'),
                meta: { title: '稿件设置' }
            }
        ]
    },
    {
        path: '/test',
        name: 'Test',
        component: () => import('@/views/test/test.vue'),
        meta: {title: '测试'}
    },
    {
        path: '/testTwo',
        name: 'TestTwo',
        component: () => import('@/views/test/TestTwo.vue'),
        meta: {title: '测试'}
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
        meta: {title: '用户空间'}
    },
    {
        path: '/chat',
        name: 'ChatView',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: {requiresAuth: true, title: '消息'}
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