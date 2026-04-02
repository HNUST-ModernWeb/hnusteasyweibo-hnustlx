<template>
  <div class="bottom-nav">
    <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
      <span class="nav-icon">🏠</span>
      <span class="nav-text">首页</span>
    </router-link>
    
    <router-link to="/discover" class="nav-item" :class="{ active: $route.path === '/discover' }">
      <span class="nav-icon">🔍</span>
      <span class="nav-text">发现</span>
    </router-link>
    
    <router-link to="/publish" class="nav-item publish-btn">
      <span class="nav-icon">+</span>
    </router-link>
    
    <router-link to="/messages" class="nav-item" :class="{ active: $route.path === '/messages' }">
      <span class="nav-icon">💬</span>
      <span class="nav-text">消息</span>
    </router-link>
    
    <router-link :to="profileLink" class="nav-item" :class="{ active: $route.path.startsWith('/profile') }">
      <span class="nav-icon">👤</span>
      <span class="nav-text">我的</span>
    </router-link>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const profileLink = computed(() => {
  const userId = localStorage.getItem('userId')
  return userId ? `/profile/${userId}` : '/login'
})
</script>

<style scoped>
.bottom-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  justify-content: space-around;
  align-items: center;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: #999;
  font-size: 10px;
  gap: 2px;
  height: 100%;
  transition: all 0.3s ease;
}

.nav-item.active {
  color: var(--primary);
}

.nav-icon {
  font-size: 20px;
}

.nav-text {
  font-size: 10px;
}

.publish-btn {
  background: var(--primary);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  margin-top: -20px;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.4);
}

.publish-btn .nav-icon {
  color: #fff;
  font-size: 24px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .bottom-nav {
    display: flex;
  }
}
</style>
