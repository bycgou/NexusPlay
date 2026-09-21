<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    getNotificationUnreadCount,
    listNotifications,
    markAllNotificationsRead,
    markNotificationRead,
    type NotificationItem
} from '@/api/notify'

const router = useRouter()

const activeTab = ref<'all' | 'unread'>('all')
const typeFilter = ref<number | undefined>(undefined)

const list = ref<NotificationItem[]>([])
const total = ref(0)
const unreadTotal = ref(0)
const page = ref(1)
const size = 20
const loading = ref(false)
const readingAll = ref(false)

// 类型元信息：文案 + 标签色 + 图标
type TagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

const typeMeta: Record<number, { label: string; tag: TagType; icon: string }> = {
    1: { label: '评论', tag: 'primary', icon: 'ChatDotRound' },
    2: { label: '回复', tag: 'success', icon: 'ChatLineRound' },
    3: { label: '关注', tag: 'warning', icon: 'User' },
    4: { label: '审核', tag: 'warning', icon: 'DocumentChecked' },
    5: { label: '直播', tag: 'danger', icon: 'VideoCamera' },
    6: { label: '系统', tag: 'info', icon: 'Bell' }
}

const metaOf = (type: number) => typeMeta[type] || { label: '通知', tag: 'info', icon: 'Bell' }

const formatTime = (value?: string) => (value ? String(value).replace('T', ' ').slice(0, 16) : '')

// 可跳转的通知：video → 视频页；live → 直播间
const jumpPath = (item: NotificationItem) => {
    if (!item.bizId) return ''
    if (item.bizType === 'video') return `/video/${item.bizId}`
    if (item.bizType === 'live') return `/live/room/${item.bizId}`
    return ''
}

const load = async () => {
    loading.value = true
    try {
        const params: { page: number; size: number; isRead?: number; type?: number } = {
            page: page.value,
            size
        }
        if (activeTab.value === 'unread') params.isRead = 0
        if (typeFilter.value) params.type = typeFilter.value

        const res: any = await listNotifications(params)
        if (res?.code === 1) {
            list.value = res.data?.records || []
            total.value = Number(res.data?.total) || 0
        } else {
            list.value = []
            total.value = 0
            ElMessage.error(res?.msg || '加载通知失败')
        }
    } catch (e) {
        // 拦截器已统一提示，这里只重置列表
        list.value = []
        total.value = 0
    } finally {
        loading.value = false
    }
}

// 刷新未读数（用于头部提示）
const loadUnreadCount = async () => {
    try {
        const res: any = await getNotificationUnreadCount()
        if (res?.code === 1) unreadTotal.value = Number(res.data) || 0
    } catch (e) {
        // 静默失败
    }
}

const handleTabChange = () => {
    page.value = 1
    load()
}

const handleFilterChange = () => {
    page.value = 1
    load()
}

const handleClickItem = async (item: NotificationItem) => {
    // 未读则先标记已读，失败不阻塞跳转
    if (item.isRead === 0) {
        try {
            const res: any = await markNotificationRead(item.id)
            if (res?.code === 1) {
                item.isRead = 1
                unreadTotal.value = Math.max(0, unreadTotal.value - 1)
                // 未读列表里标记已读后移除该条，避免停留在已读内容上
                if (activeTab.value === 'unread') {
                    list.value = list.value.filter((row) => row.id !== item.id)
                    total.value = Math.max(0, total.value - 1)
                }
            } else {
                ElMessage.error(res?.msg || '标记已读失败')
            }
        } catch (e) {
            // 拦截器已提示
        }
    }

    const path = jumpPath(item)
    if (path) {
        router.push(path)
    }
}

const handleReadAll = async () => {
    if (unreadTotal.value === 0) {
        ElMessage.info('没有未读通知')
        return
    }
    try {
        await ElMessageBox.confirm('确定将全部通知标记为已读吗？', '全部已读', {
            type: 'warning',
            confirmButtonText: '确定',
            cancelButtonText: '取消'
        })
    } catch {
        // 用户取消
        return
    }

    readingAll.value = true
    try {
        const res: any = await markAllNotificationsRead()
        if (res?.code === 1) {
            ElMessage.success('已全部标记为已读')
            unreadTotal.value = 0
            await load()
        } else {
            ElMessage.error(res?.msg || '操作失败')
        }
    } catch (e) {
        // 拦截器已提示
    } finally {
        readingAll.value = false
    }
}

const unreadHint = computed(() => (unreadTotal.value > 0 ? `未读 ${unreadTotal.value} 条` : '暂无未读通知'))

onMounted(() => {
    load()
    loadUnreadCount()
})
</script>

<template>
  <div class="notification-page">
    <div class="page-header">
      <div>
        <h2>通知中心</h2>
        <p class="sub">{{ unreadHint }}</p>
      </div>
      <div class="header-actions">
        <el-select
            v-model="typeFilter"
            placeholder="全部类型"
            clearable
            class="type-select"
            @change="handleFilterChange"
        >
          <el-option label="评论" :value="1" />
          <el-option label="回复" :value="2" />
          <el-option label="关注" :value="3" />
          <el-option label="审核" :value="4" />
          <el-option label="直播开播" :value="5" />
          <el-option label="系统" :value="6" />
        </el-select>
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" :loading="readingAll" @click="handleReadAll">全部已读</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="tabs" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="未读" name="unread" />
    </el-tabs>

    <el-empty v-if="!loading && list.length === 0" description="暂无通知">
      <el-button @click="load">刷新</el-button>
    </el-empty>

    <div v-else v-loading="loading" class="list">
      <div
          v-for="item in list"
          :key="item.id"
          class="item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleClickItem(item)"
      >
        <div class="icon-wrap">
          <el-icon size="18"><component :is="metaOf(item.type).icon" /></el-icon>
        </div>

        <div class="body">
          <div class="title-row">
            <span class="title">{{ item.title }}</span>
            <el-tag size="small" :type="metaOf(item.type).tag" effect="light">
              {{ metaOf(item.type).label }}
            </el-tag>
            <span v-if="item.isRead === 0" class="unread-dot">未读</span>
          </div>
          <p v-if="item.content" class="content">{{ item.content }}</p>
          <div class="meta">
            <span>{{ formatTime(item.createTime) }}</span>
            <span v-if="jumpPath(item)" class="link">
              查看详情<el-icon size="12"><ArrowRight /></el-icon>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > size" class="pager">
      <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="(p: number) => { page = p; load() }"
      />
    </div>
  </div>
</template>

<style scoped>
.notification-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: var(--ink);
}
.sub {
  margin: 4px 0 0;
  color: var(--mist);
  font-size: 13px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.type-select {
  width: 140px;
}
.tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 120px;
}
.item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--line);
  background: var(--paper-white);
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}
.item:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}
.item.unread {
  border-color: var(--brand-light);
  background: var(--brand-bg);
}
.icon-wrap {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--brand-bg);
  color: var(--brand);
}
.body {
  flex: 1;
  min-width: 0;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.title {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
.unread-dot {
  font-size: 12px;
  color: var(--like);
}
.content {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--ink-secondary);
  word-break: break-word;
}
.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--mist);
}
.link {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--brand);
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
