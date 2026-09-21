<template>
  <div class="settings-container">
    <!-- 顶部导航栏 -->
    <header class="settings-header">
      <div class="user-info">
        <img
            src="https://picsum.photos/id/1005/40/40"
            alt="用户头像"
            class="user-avatar"
        >
        <div class="user-details">
          <h2>欢迎回来，管理员</h2>
          <p>这里是您的内容管理中心</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn-help">帮助中心</button>
        <button class="btn-logout">退出登录</button>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <div class="settings-content">
      <!-- 左侧菜单 -->
      <aside class="settings-sidebar">
        <nav class="sidebar-menu">
          <div class="menu-section">
            <h3 class="section-title">内容管理</h3>
            <ul class="menu-items">
              <li class="menu-item active">
                <a href="#" class="item-link">
                  <i class="icon material-icons">article</i>
                  <span class="item-text">稿件管理</span>
                </a>
              </li>
              <li class="menu-item">
                <a href="#" class="item-link">
                  <i class="icon material-icons">schedule</i>
                  <span class="item-text">发布计划</span>
                </a>
              </li>
              <li class="menu-item">
                <a href="#" class="item-link">
                  <i class="icon material-icons">insert_drive_file</i>
                  <span class="item-text">素材库</span>
                </a>
              </li>
            </ul>
          </div>

          <div class="menu-section">
            <h3 class="section-title">账户设置</h3>
            <ul class="menu-items">
              <li class="menu-item">
                <a href="#" class="item-link">
                  <i class="icon material-icons">person</i>
                  <span class="item-text">个人信息</span>
                </a>
              </li>
              <li class="menu-item">
                <a href="#" class="item-link">
                  <i class="icon material-icons">security</i>
                  <span class="item-text">安全设置</span>
                </a>
              </li>
              <li class="menu-item">
                <a href="#" class="item-link">
                  <i class="icon material-icons">notifications</i>
                  <span class="item-text">通知管理</span>
                </a>
              </li>
            </ul>
          </div>

          <div class="menu-section">
            <h3 class="section-title">系统设置</h3>
            <ul class="menu-items">
              <li class="menu-item">
                <a href="#" class="item-link">
                  <i class="icon material-icons">settings</i>
                  <span class="item-text">网站设置</span>
                </a>
              </li>
              <li class="menu-item">
                <a href="#" class="item-link">
                  <i class="icon material-icons">group</i>
                  <span class="item-text">用户管理</span>
                </a>
              </li>
              <li class="menu-item">
                <a href="#" class="item-link">
                  <i class="icon material-icons">bar_chart</i>
                  <span class="item-text">数据统计</span>
                </a>
              </li>
            </ul>
          </div>
        </nav>
      </aside>

      <!-- 右侧主内容 -->
      <main class="settings-main">
        <div class="content-header">
          <h1>稿件管理</h1>
          <div class="content-actions">
            <button class="btn-filter">
              <i class="material-icons">filter_list</i>
              筛选
            </button>
            <button class="btn-export">
              <i class="material-icons">file_download</i>
              导出
            </button>
            <button class="btn-create primary">
              <i class="material-icons">add</i>
              新建稿件
            </button>
          </div>
        </div>

        <!-- 稿件搜索和筛选 -->
        <div class="search-bar">
          <div class="search-input">
            <i class="material-icons">search</i>
            <input type="text" placeholder="搜索稿件标题、ID或作者">
          </div>
          <div class="filter-options">
            <select class="filter-select">
              <option value="">全部状态</option>
              <option value="draft">草稿</option>
              <option value="pending">待审核</option>
              <option value="published">已发布</option>
              <option value="rejected">已驳回</option>
            </select>
            <select class="filter-select">
              <option value="">全部分类</option>
              <option value="news">新闻</option>
              <option value="tech">科技</option>
              <option value="entertainment">娱乐</option>
              <option value="sports">体育</option>
            </select>
            <select class="filter-select">
              <option value="">全部时间</option>
              <option value="today">今天</option>
              <option value="week">本周</option>
              <option value="month">本月</option>
              <option value="quarter">本季度</option>
            </select>
          </div>
        </div>

        <!-- 稿件列表 -->
        <div class="articles-table">
          <table>
            <thead>
            <tr>
              <th class="checkbox-col">
                <input type="checkbox" id="select-all">
              </th>
              <th>ID</th>
              <th>标题</th>
              <th>作者</th>
              <th>分类</th>
              <th>状态</th>
              <th>发布时间</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr class="article-row">
              <td class="checkbox-col">
                <input type="checkbox">
              </td>
              <td>ART-2023-1001</td>
              <td class="title">
                <div class="article-title">
                  <img src="https://picsum.photos/id/237/60/40" alt="缩略图" class="article-thumb">
                  <span>2023年视频网站发展趋势分析报告</span>
                </div>
              </td>
              <td>张三</td>
              <td>科技</td>
              <td>
                <span class="status published">已发布</span>
              </td>
              <td>2023-06-15 14:30</td>
              <td class="actions">
                <button class="action-btn view">
                  <i class="material-icons">visibility</i>
                </button>
                <button class="action-btn edit">
                  <i class="material-icons">edit</i>
                </button>
                <button class="action-btn delete">
                  <i class="material-icons">delete</i>
                </button>
              </td>
            </tr>
            <tr class="article-row">
              <td class="checkbox-col">
                <input type="checkbox">
              </td>
              <td>ART-2023-1002</td>
              <td class="title">
                <div class="article-title">
                  <img src="https://picsum.photos/id/119/60/40" alt="缩略图" class="article-thumb">
                  <span>如何制作吸引人的短视频内容</span>
                </div>
              </td>
              <td>李四</td>
              <td>娱乐</td>
              <td>
                <span class="status pending">待审核</span>
              </td>
              <td>2023-06-16 09:45</td>
              <td class="actions">
                <button class="action-btn view">
                  <i class="material-icons">visibility</i>
                </button>
                <button class="action-btn edit">
                  <i class="material-icons">edit</i>
                </button>
                <button class="action-btn delete">
                  <i class="material-icons">delete</i>
                </button>
              </td>
            </tr>
            <tr class="article-row">
              <td class="checkbox-col">
                <input type="checkbox">
              </td>
              <td>ART-2023-1003</td>
              <td class="title">
                <div class="article-title">
                  <img src="https://picsum.photos/id/180/60/40" alt="缩略图" class="article-thumb">
                  <span>视频剪辑技巧大全：从入门到精通</span>
                </div>
              </td>
              <td>王五</td>
              <td>科技</td>
              <td>
                <span class="status draft">草稿</span>
              </td>
              <td>2023-06-17 11:20</td>
              <td class="actions">
                <button class="action-btn view">
                  <i class="material-icons">visibility</i>
                </button>
                <button class="action-btn edit">
                  <i class="material-icons">edit</i>
                </button>
                <button class="action-btn delete">
                  <i class="material-icons">delete</i>
                </button>
              </td>
            </tr>
            <tr class="article-row">
              <td class="checkbox-col">
                <input type="checkbox">
              </td>
              <td>ART-2023-1004</td>
              <td class="title">
                <div class="article-title">
                  <img src="https://picsum.photos/id/250/60/40" alt="缩略图" class="article-thumb">
                  <span>2023年最受欢迎的视频拍摄设备推荐</span>
                </div>
              </td>
              <td>赵六</td>
              <td>科技</td>
              <td>
                <span class="status rejected">已驳回</span>
              </td>
              <td>2023-06-18 16:10</td>
              <td class="actions">
                <button class="action-btn view">
                  <i class="material-icons">visibility</i>
                </button>
                <button class="action-btn edit">
                  <i class="material-icons">edit</i>
                </button>
                <button class="action-btn delete">
                  <i class="material-icons">delete</i>
                </button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <div class="pagination-info">
            显示 1-4 条，共 24 条
          </div>
          <div class="pagination-controls">
            <button class="page-btn prev disabled">上一页</button>
            <button class="page-btn active">1</button>
            <button class="page-btn">2</button>
            <button class="page-btn">3</button>
            <button class="page-btn">4</button>
            <button class="page-btn">5</button>
            <button class="page-btn next">下一页</button>
          </div>
        </div>
      </main>
    </div>

    <!-- 页脚 -->
    <footer class="settings-footer">
      <div class="footer-left">
        © 2025 视频网站管理系统
      </div>
      <div class="footer-right">
        <a href="#">使用帮助</a>
        <a href="#">隐私政策</a>
        <a href="#">联系我们</a>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// 可以在这里添加交互逻辑
const activeMenu = ref('articles');

const changeMenu = (menu) => {
  activeMenu.value = menu;
};
</script>

<style scoped>
/* 全局样式 */
.settings-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
  color: #333;
  font-family: 'Roboto', sans-serif;
}

/* 顶部样式 */
.settings-header {
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  padding: 0 20px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 100;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0f0f0;
}

.user-details h2 {
  font-size: 16px;
  font-weight: 500;
  margin: 0;
}

.user-details p {
  font-size: 13px;
  color: #666;
  margin: 4px 0 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-help, .btn-logout {
  background: none;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-help {
  color: #333;
}

.btn-help:hover {
  background-color: #f0f0f0;
}

.btn-logout {
  color: #d32f2f;
}

.btn-logout:hover {
  background-color: #ffebee;
}

/* 内容区域样式 */
.settings-content {
  display: flex;
  flex: 1;
  margin: 20px;
  gap: 20px;
}

/* 侧边栏样式 */
.settings-sidebar {
  width: 240px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  padding: 20px 0;
  flex-shrink: 0;
}

.sidebar-menu {
  padding: 0 10px;
}

.menu-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 12px;
  text-transform: uppercase;
  color: #999;
  padding: 0 16px 8px;
  letter-spacing: 1px;
  font-weight: 500;
}

.menu-items {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-item {
  margin-bottom: 4px;
  border-radius: 6px;
  transition: all 0.2s;
}

.menu-item.active {
  background-color: #f0f5ff;
}

.menu-item.active .item-link {
  color: #1890ff;
}

.item-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.item-link:hover {
  color: #1890ff;
}

.icon {
  font-size: 20px;
  width: 24px;
  text-align: center;
}

/* 主内容区域样式 */
.settings-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-header h1 {
  font-size: 24px;
  font-weight: 500;
  margin: 0;
}

.content-actions {
  display: flex;
  gap: 12px;
}

/* 按钮样式 */
.btn-filter, .btn-export, .btn-create {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 4px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-filter {
  background-color: #fff;
  border: 1px solid #ddd;
  color: #333;
}

.btn-filter:hover {
  background-color: #f5f5f5;
}

.btn-export {
  background-color: #fff;
  border: 1px solid #ddd;
  color: #333;
}

.btn-export:hover {
  background-color: #f5f5f5;
}

.btn-create.primary {
  background-color: #1890ff;
  color: #fff;
}

.btn-create.primary:hover {
  background-color: #096dd9;
}

/* 搜索栏样式 */
.search-bar {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 200px;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 12px;
  height: 40px;
}

.search-input input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  height: 100%;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-select {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 12px;
  height: 40px;
  background-color: #fff;
  outline: none;
  font-size: 14px;
  min-width: 120px;
}

/* 稿件列表样式 */
.articles-table {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background-color: #f5f7fa;
}

th, td {
  padding: 12px 16px;
  text-align: left;
  font-size: 14px;
}

th {
  font-weight: 500;
  color: #333;
  border-bottom: 1px solid #ddd;
}

tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

tbody tr:hover {
  background-color: #fafafa;
}

.checkbox-col input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.title .article-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.article-thumb {
  width: 60px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.status {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status.published {
  background-color: #e6f7ef;
  color: #00a86b;
}

.status.pending {
  background-color: #fff7e6;
  color: #ff7d00;
}

.status.draft {
  background-color: #e8f4ff;
  color: #1890ff;
}

.status.rejected {
  background-color: #ffebee;
  color: #d32f2f;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: none;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.view {
  color: #1890ff;
}

.action-btn.view:hover {
  background-color: #e8f4ff;
}

.action-btn.edit {
  color: #ff7d00;
}

.action-btn.edit:hover {
  background-color: #fff7e6;
}

.action-btn.delete {
  color: #d32f2f;
}

.action-btn.delete:hover {
  background-color: #ffebee;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
}

.pagination-info {
  font-size: 14px;
  color: #666;
}

.pagination-controls {
  display: flex;
  gap: 8px;
}

.page-btn {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  border: 1px solid #ddd;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.page-btn:hover:not(.disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.page-btn.active {
  background-color: #1890ff;
  color: #fff;
  border-color: #1890ff;
}

.page-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 页脚样式 */
.settings-footer {
  background-color: #fff;
  padding: 16px 20px;
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  border-top: 1px solid #f0f0f0;
}

.footer-left {
  font-size: 14px;
  color: #666;
}

.footer-right {
  display: flex;
  gap: 20px;
}

.footer-right a {
  font-size: 14px;
  color: #666;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-right a:hover {
  color: #1890ff;
}

/* 响应式样式 */
@media (max-width: 992px) {
  .settings-content {
    flex-direction: column;
  }

  .settings-sidebar {
    width: 100%;
    margin-bottom: 20px;
  }

  .sidebar-menu {
    display: flex;
    overflow-x: auto;
    padding-bottom: 10px;
  }

  .menu-section {
    margin-right: 24px;
    margin-bottom: 0;
  }

  .menu-items {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
}

@media (max-width: 768px) {
  .settings-header {
    flex-direction: column;
    height: auto;
    padding: 12px 20px;
    gap: 12px;
  }

  .user-info, .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .content-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>