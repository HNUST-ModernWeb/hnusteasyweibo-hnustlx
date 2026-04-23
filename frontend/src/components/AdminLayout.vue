<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <img src="/logo.png" class="logo-img" alt="logo" />
        <span class="title">科大微博</span>
      </div>
      
      <nav class="sidebar-nav">
        <router-link to="/admin/dashboard" class="nav-item" :class="{ active: $route.path === '/admin/dashboard' }">
          <span class="nav-icon">📊</span>
          <span class="nav-text">仪表板</span>
        </router-link>
        
        <router-link to="/admin/users" class="nav-item" :class="{ active: $route.path === '/admin/users' }">
          <span class="nav-icon">👥</span>
          <span class="nav-text">用户管理</span>
        </router-link>
        
        <router-link to="/admin/tags" class="nav-item" :class="{ active: $route.path === '/admin/tags' }">
          <span class="nav-icon">🏷️</span>
          <span class="nav-text">标签管理</span>
        </router-link>
      </nav>
      
      <div class="sidebar-footer">
        <router-link to="/" class="nav-item">
          <span class="nav-icon">🏠</span>
          <span class="nav-text">返回首页</span>
        </router-link>
      </div>
    </aside>
    
    <main class="main-content">
      <header class="header">
        <h1 class="page-title">{{ pageTitle }}</h1>
        <div class="header-actions">
          <span class="admin-name">管理员: {{ adminUsername }}</span>
          <button class="logout-btn" @click="handleLogout">安全退出</button>
        </div>
      </header>
      
      <div class="content-area">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const adminUsername = computed(() => localStorage.getItem('admin_username') || '管理员')

const pageTitle = computed(() => {
  const titles = {
    '/admin/dashboard': '仪表板',
    '/admin/users': '用户管理',
    '/admin/tags': '标签管理'
  }
  return titles[route.path] || '管理后台'
})

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
    router.push('/admin/login')
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

.sidebar {
  width: 220px;
  background: #1a1a2e;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.logo {
  font-size: 24px;
}

.title {
  font-size: 18px;
  font-weight: 700;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 10px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  border-radius: 10px;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-item.active {
  background: #1890ff;
  color: #fff;
}

.nav-icon {
  font-size: 18px;
}

.nav-text {
  font-size: 15px;
}

.sidebar-footer {
  padding: 20px 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.main-content {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  padding: 20px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 50;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-name {
  font-size: 14px;
  color: #666;
}

.logout-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: #fff;
}

.content-area {
  flex: 1;
  padding: 24px 30px;
}

@media (max-width: 768px) {
  .sidebar {
    width: 60px;
  }
  
  .sidebar-header .title,
  .nav-text {
    display: none;
  }
  
  .nav-item {
    justify-content: center;
    padding: 14px;
  }
  
  .main-content {
    margin-left: 60px;
  }
}
</style>
