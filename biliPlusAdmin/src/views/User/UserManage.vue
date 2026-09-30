<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  banMember,
  getMemberPenalties,
  getMembers,
  muteMember,
  unbanMember,
  unmuteMember,
  updateMemberRole,
  type AdminMember,
  type MemberPenaltiesResult,
  type MemberPenalty
} from '@/api/member'

const loading = ref(false)
const list = ref<AdminMember[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  role: undefined as number | undefined
})

// 处置弹窗
const actionVisible = ref(false)
const actionType = ref<'ban' | 'mute'>('ban')
const actionForm = reactive({ reason: '', days: undefined as number | undefined })
const actionTarget = ref<AdminMember | null>(null)
const actionLoading = ref(false)

// 详情 / 处置记录
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailUser = ref<AdminMember | null>(null)
const penaltyData = ref<MemberPenaltiesResult | null>(null)

const roleText = (r?: number) => {
  if (r === 1) return 'UP主'
  if (r === 2) return '管理员'
  return '普通用户'
}

const roleTagType = (r?: number) => {
  if (r === 2) return 'danger'
  if (r === 1) return 'success'
  return 'info'
}

const statusText = (s?: number) => (s === 1 ? '正常' : '禁用')
const statusTagType = (s?: number) => (s === 1 ? 'success' : 'danger')

const creditTagType = (score?: number) => {
  const n = Number(score ?? 100)
  if (n < 30) return 'danger'
  if (n < 60) return 'warning'
  return 'success'
}

const penaltyStatusText = (s?: number) => {
  if (s === 1) return '生效中'
  if (s === 2) return '已到期'
  if (s === 3) return '已提前解除'
  return '未知'
}

const penaltyActionText = (a?: string) => (a === 'mute' ? '禁言' : a === 'ban' ? '封禁' : '未知')
const endTimeText = (row: MemberPenalty) => (row.endTime ? row.endTime : '永久')

const loadList = async () => {
  loading.value = true
  try {
    const res: any = await getMembers({
      keyword: query.keyword?.trim() || undefined,
      status: query.status,
      role: query.role,
      page: page.value,
      size: size.value
    })
    if (res?.code === 1) {
      list.value = res.data?.records || []
      total.value = Number(res.data?.total) || 0
    } else {
      list.value = []
      total.value = 0
    }
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadList()
}

const handleReset = () => {
  query.keyword = ''
  query.status = undefined
  query.role = undefined
  handleSearch()
}

const openDetail = async (row: AdminMember) => {
  detailUser.value = { ...row }
  detailVisible.value = true
  detailLoading.value = true
  penaltyData.value = null
  try {
    const res: any = await getMemberPenalties(row.id)
    if (res?.code === 1) {
      penaltyData.value = res.data
    }
  } catch (e) {
    // 拦截器已提示
  } finally {
    detailLoading.value = false
  }
}

const openAction = (type: 'ban' | 'mute', row: AdminMember) => {
  actionType.value = type
  actionTarget.value = { ...row }
  actionForm.reason = ''
  actionForm.days = undefined
  actionVisible.value = true
}

const submitAction = async () => {
  if (!actionTarget.value) return
  actionLoading.value = true
  try {
    const payload = {
      reason: actionForm.reason?.trim() || undefined,
      days: actionForm.days
    }
    const res: any =
      actionType.value === 'ban'
        ? await banMember(actionTarget.value.id, payload)
        : await muteMember(actionTarget.value.id, payload)
    if (res?.code === 1) {
      ElMessage.success(actionType.value === 'ban' ? '已封禁' : '已禁言')
      actionVisible.value = false
      await loadList()
    }
  } catch (e) {
    // 拦截器已提示
  } finally {
    actionLoading.value = false
  }
}

const handleRelease = async (row: AdminMember, action: 'ban' | 'mute') => {
  const label = action === 'ban' ? '解封' : '解除禁言'
  try {
    await ElMessageBox.confirm(`确认对「${row.nickname || row.username}」执行${label}？`, '提示', {
      type: 'warning'
    })
    const res: any = action === 'ban' ? await unbanMember(row.id) : await unmuteMember(row.id)
    if (res?.code === 1) {
      ElMessage.success(`已${label}`)
      await loadList()
    }
  } catch (e) {
    if (e !== 'cancel') {
      // 拦截器已提示
    }
  }
}

const handleRoleChange = async (row: AdminMember) => {
  try {
    await ElMessageBox.confirm(
      `将「${row.nickname || row.username}」角色调整为「${roleText(row.role)}」？`,
      '调整角色',
      { type: 'warning' }
    )
    const res: any = await updateMemberRole(row.id, row.role)
    if (res?.code === 1) {
      ElMessage.success('角色已更新')
      await loadList()
    }
  } catch (e) {
    await loadList()
  }
}

onMounted(loadList)
</script>

<template>
  <div class="page">
    <el-form inline class="filters">
      <el-form-item label="关键词">
        <el-input
            v-model="query.keyword"
            placeholder="用户名 / 昵称 / 邮箱 / 手机 / UID"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="query.role" placeholder="全部" clearable style="width: 120px">
          <el-option label="普通用户" :value="0" />
          <el-option label="UP主" :value="1" />
          <el-option label="管理员" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="loadList">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe border style="width: 100%">
      <el-table-column prop="id" label="UID" width="80" />
      <el-table-column label="用户" min-width="180">
        <template #default="{ row }">
          <div class="user-cell">
            <el-avatar :size="32" :src="row.avatar || ''" />
            <div class="user-meta">
              <div class="uname">{{ row.nickname || row.username }}</div>
              <div class="umail">{{ row.email || row.username }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="roleTagType(row.role)">{{ roleText(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="信用分" width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="creditTagType(row.creditScore)">
            {{ row.creditScore ?? 100 }}
          </el-tag>
          <span class="muted-text" v-if="row.violationCount"> / 违规{{ row.violationCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="投稿/粉丝/关注" width="140">
        <template #default="{ row }">
          {{ row.videoCount || 0 }} / {{ row.fansCount || 0 }} / {{ row.followingCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="处置" width="140">
        <template #default="{ row }">
          <el-tag v-if="row.banned" size="small" type="danger" class="tag-gap">封禁中</el-tag>
          <el-tag v-if="row.muted" size="small" type="warning">禁言中</el-tag>
          <span v-if="!row.banned && !row.muted" class="muted-text">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">详情</el-button>
          <el-button size="small" type="warning" @click="openAction('mute', row)">禁言</el-button>
          <el-button v-if="row.muted" size="small" type="success" @click="handleRelease(row, 'mute')">
            解禁
          </el-button>
          <el-button size="small" type="danger" @click="openAction('ban', row)">封禁</el-button>
          <el-button v-if="row.banned" size="small" type="success" @click="handleRelease(row, 'ban')">
            解封
          </el-button>
          <el-select
              v-model="row.role"
              size="small"
              class="role-select"
              @change="handleRoleChange(row)"
          >
            <el-option label="普通" :value="0" />
            <el-option label="UP主" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
          layout="total, prev, pager, next, sizes"
          :total="total"
          :page-size="size"
          :current-page="page"
          :page-sizes="[10, 20, 50]"
          @current-change="(p: number) => { page = p; loadList() }"
          @size-change="(s: number) => { size = s; page = 1; loadList() }"
      />
    </div>

    <!-- 禁言 / 封禁 -->
    <el-dialog
        v-model="actionVisible"
        :title="actionType === 'ban' ? '封禁用户' : '禁言用户'"
        width="480px"
     append-to-body>
      <p class="dialog-tip">
        目标：UID {{ actionTarget?.id }} · {{ actionTarget?.nickname || actionTarget?.username }}
      </p>
      <el-form label-width="90px">
        <el-form-item label="原因">
          <el-input
              v-model="actionForm.reason"
              type="textarea"
              :rows="3"
              maxlength="200"
              placeholder="填写处置原因，便于审计"
          />
        </el-form-item>
        <el-form-item label="时长(天)">
          <el-input-number v-model="actionForm.days" :min="1" :max="365" placeholder="空=永久" />
          <span class="muted-text">不填表示永久</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button
            type="danger"
            :loading="actionLoading"
            @click="submitAction"
        >
          确认{{ actionType === 'ban' ? '封禁' : '禁言' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="640px" append-to-body>
      <div v-loading="detailLoading">
        <el-descriptions v-if="detailUser" :column="2" border>
          <el-descriptions-item label="UID">{{ detailUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ detailUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailUser.nickname || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailUser.email || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ detailUser.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ roleText(detailUser.role) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(detailUser.status) }}</el-descriptions-item>
          <el-descriptions-item label="信用分">
            {{ detailUser.creditScore ?? 100 }}
            <span class="muted-text">（违规 {{ detailUser.violationCount || 0 }} 次）</span>
          </el-descriptions-item>
          <el-descriptions-item label="投稿 / 粉丝 / 关注">
            {{ detailUser.videoCount || 0 }} / {{ detailUser.fansCount || 0 }} / {{ detailUser.followingCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ detailUser.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="签名" :span="2">
            {{ detailUser.signature || '—' }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="section-title">处置记录</h4>
        <div v-if="penaltyData" class="penalty-tags">
          <el-tag v-if="penaltyData.activeBan" type="danger" class="tag-gap">生效中 · 封禁</el-tag>
          <el-tag v-if="penaltyData.activeMute" type="warning" class="tag-gap">生效中 · 禁言</el-tag>
          <span v-if="!penaltyData.activeBan && !penaltyData.activeMute" class="muted-text">
            当前无生效处置
          </span>
        </div>
        <el-table
            v-if="penaltyData?.list?.records?.length"
            :data="penaltyData.list.records"
            size="small"
            border
            max-height="240"
        >
          <el-table-column prop="action" label="类型" width="80">
            <template #default="{ row }">{{ penaltyActionText(row.action) }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" min-width="140" show-overflow-tooltip />
          <el-table-column prop="startTime" label="开始" width="160" />
          <el-table-column label="结束" width="160">
            <template #default="{ row }">{{ endTimeText(row) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'danger' : 'info'">
                {{ penaltyStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  padding: 12px;
}
.filters {
  margin-bottom: 8px;
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-meta {
  min-width: 0;
}
.uname {
  font-weight: 600;
  font-size: 13px;
}
.umail {
  color: #909399;
  font-size: 12px;
}
.muted-text {
  color: #909399;
  font-size: 12px;
}
.tag-gap {
  margin-right: 6px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.dialog-tip {
  margin: 0 0 12px;
  color: #606266;
}
.section-title {
  margin: 16px 0 8px;
}
.penalty-tags {
  margin-bottom: 8px;
}
.role-select {
  width: 96px;
  margin-left: 8px;
  vertical-align: middle;
}
</style>
