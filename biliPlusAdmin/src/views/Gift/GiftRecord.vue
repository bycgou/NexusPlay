<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getGiftRecords, type GiftRecord } from '@/api/live'

const loading = ref(false)
const list = ref<GiftRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const roomId = ref<number | undefined>()
const senderId = ref<number | undefined>()
const hostUserId = ref<number | undefined>()

const loadList = async () => {
  loading.value = true
  try {
    const res = await getGiftRecords({
      roomId: roomId.value || undefined,
      senderId: senderId.value || undefined,
      hostUserId: hostUserId.value || undefined,
      page: page.value,
      size: size
    })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  page.value = 1
  loadList()
}

onMounted(loadList)
</script>

<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model.number="roomId" placeholder="房间ID" clearable style="width: 140px" />
      <el-input v-model.number="senderId" placeholder="送礼用户ID" clearable style="width: 140px" />
      <el-input v-model.number="hostUserId" placeholder="主播ID" clearable style="width: 140px" />
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="liveRoomId" label="房间" width="80" />
      <el-table-column prop="giftId" label="礼物ID" width="80" />
      <el-table-column prop="senderId" label="送礼人" width="90" />
      <el-table-column prop="hostUserId" label="主播" width="90" />
      <el-table-column prop="unitPrice" label="单价" width="80" />
      <el-table-column prop="count" label="数量" width="70" />
      <el-table-column prop="totalPrice" label="总价" width="90" />
      <el-table-column prop="createTime" label="时间" min-width="160" />
    </el-table>

    <div class="pager">
      <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadList"
      />
    </div>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; flex-wrap: wrap; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
