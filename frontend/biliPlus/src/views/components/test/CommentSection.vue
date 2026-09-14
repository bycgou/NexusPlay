<script setup lang="ts">
// <!--评论区,(输入框+评论列表-->
import { ref } from 'vue'

const comments = ref([
  { id: 1, user: { name: '天呐噜啊啊', avatar: 'https://picsum.photos/50/50?random=3' }, content: '网友反嘴你', likeCount: 12 },
  { id: 2, user: { name: '原来不断有人处的好的家庭好少一啊', avatar: 'https://picsum.photos/50/50?random=4' }, content: '2020-11-16 11:30', likeCount: 8 }
])
</script>

<template>
  <div class="comment-section" style="background: #fff; padding: 16px; border-radius: 8px; margin-top: 16px;">
    <h3 class="comment-title" style="font-size: 18px; margin-bottom: 16px;">评论区（{{ comments.length }}）</h3>

    <div class="comment-input" style="margin-bottom: 20px;">
      <el-input
          type="textarea"
          rows="4"
          placeholder="写下你的评论..."
          style="width: 100%; margin-bottom: 10px;"
      />
      <el-button type="primary">发送</el-button>
    </div>

    <div class="comment-list">
      <div class="comment-item" v-for="comment in comments" :key="comment.id" style="display: flex; gap: 10px; padding: 10px 0; border-bottom: 1px solid #eee;">
        <img :src="comment.user.avatar" class="comment-avatar" style="width: 40px; height: 40px; border-radius: 50%;" />
        <div class="comment-content">
          <div class="comment-user" style="font-weight: 500; margin-bottom: 4px; font-size: 14px;">{{ comment.user.name }}</div>
          <div class="comment-text" style="margin-bottom: 8px; line-height: 1.5;">{{ comment.content }}</div>
          <div class="comment-actions" style="display: flex; gap: 16px; font-size: 12px; color: #999;">
            <span class="like">{{ comment.likeCount }} 赞</span>
            <span class="reply">回复</span>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<style scoped>

</style>