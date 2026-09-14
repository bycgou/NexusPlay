<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const currentYear = new Date().getFullYear()

const footerLinks = [
  {
    title: '关于',
    links: [
      { name: '关于我们', url: '#' },
      { name: '联系我们', url: '#' },
      { name: '加入我们', url: '#' }
    ]
  },
  {
    title: '帮助',
    links: [
      { name: '用户协议', url: '#' },
      { name: '隐私政策', url: '#' },
      { name: '帮助中心', url: '#' }
    ]
  },
  {
    title: '合作',
    links: [
      { name: '商务合作', url: '#' },
      { name: '广告投放', url: '#' },
      { name: 'UP主入驻', url: '#' }
    ]
  }
]

const socialLinks = [
  { name: 'GitHub', icon: 'Link', url: '#' },
  { name: 'Bilibili', icon: 'VideoPlay', url: '#' },
  { name: '微博', icon: 'Share', url: '#' }
]
</script>

<template>
  <footer class="app-footer">
    <div class="footer-content">
      <!-- 主要内容区 -->
      <div class="footer-main">
        <!-- 品牌区 -->
        <div class="footer-brand">
          <div class="brand-logo">
            <img src="/favicon.ico" alt="NexusPlay" class="logo-icon" />
            <span class="logo-text">NexusPlay</span>
          </div>
          <p class="brand-desc">
            发现精彩视频，分享创意生活
          </p>
          <div class="social-links">
            <a
                v-for="social in socialLinks"
                :key="social.name"
                :href="social.url"
                class="social-link"
                :title="social.name"
                target="_blank"
                rel="noopener"
            >
              <el-icon><component :is="social.icon" /></el-icon>
            </a>
          </div>
        </div>

        <!-- 链接区 -->
        <div class="footer-links">
          <div v-for="group in footerLinks" :key="group.title" class="link-group">
            <h4 class="link-title">{{ group.title }}</h4>
            <ul class="link-list">
              <li v-for="link in group.links" :key="link.name">
                <a :href="link.url" class="link-item">{{ link.name }}</a>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 底部版权区 -->
      <div class="footer-bottom">
        <div class="copyright">
          <span>&copy; {{ currentYear }} NexusPlay. All rights reserved.</span>
        </div>
        <div class="icp">
          <a href="#" class="icp-link">京ICP备XXXXXXXX号</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: var(--ink);
  color: rgba(255, 255, 255, 0.7);
  margin-top: var(--space-2xl);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-2xl) var(--space-md) var(--space-lg);
}

.footer-main {
  display: flex;
  justify-content: space-between;
  gap: var(--space-2xl);
  padding-bottom: var(--space-xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 品牌区 */
.footer-brand {
  max-width: 280px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-md);
}

.logo-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: white;
  letter-spacing: -0.5px;
}

.brand-desc {
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.5);
  margin: 0 0 var(--space-lg);
}

.social-links {
  display: flex;
  gap: var(--space-sm);
}

.social-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.6);
  transition: all var(--transition-fast);
}

.social-link:hover {
  background: var(--brand);
  color: white;
  transform: translateY(-2px);
}

/* 链接区 */
.footer-links {
  display: flex;
  gap: var(--space-2xl);
}

.link-group {
  min-width: 100px;
}

.link-title {
  font-size: 14px;
  font-weight: 600;
  color: white;
  margin: 0 0 var(--space-md);
}

.link-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.link-item {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  transition: color var(--transition-fast);
}

.link-item:hover {
  color: var(--brand-light);
}

/* 底部版权区 */
.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-lg);
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.icp-link {
  color: rgba(255, 255, 255, 0.4);
  transition: color var(--transition-fast);
}

.icp-link:hover {
  color: rgba(255, 255, 255, 0.7);
}

/* 响应式 */
@media (max-width: 768px) {
  .footer-main {
    flex-direction: column;
    gap: var(--space-xl);
  }

  .footer-brand {
    max-width: 100%;
  }

  .footer-links {
    flex-wrap: wrap;
    gap: var(--space-xl);
  }

  .link-group {
    min-width: 80px;
  }

  .footer-bottom {
    flex-direction: column;
    gap: var(--space-sm);
    text-align: center;
  }
}
</style>
