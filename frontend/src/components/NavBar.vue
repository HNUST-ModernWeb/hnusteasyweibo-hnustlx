<template>
  <nav class="navbar">
    <div class="nav-container">
      <router-link to="/" class="logo">
        <img src="/logo.png" class="logo-img" alt="logo" />
        <span class="logo-text">科大微博</span>
      </router-link>
      
      <div class="search-box">
        <input 
          v-model="searchKeyword"
          type="text" 
          placeholder="搜索用户或帖子..."
          @keyup.enter="doSearch"
        />
        <span class="search-icon" @click="doSearch">🔍</span>
      </div>
      
      <div class="nav-actions">
        <router-link to="/messages" class="message-link">
          <span class="icon">💬</span>
          <span>消息</span>
        </router-link>
        
        <router-link to="/publish" class="publish-btn">
          <span class="icon">✏️</span>
          <span>发布</span>
        </router-link>
        
        <div class="user-menu" v-if="username">
          <img 
            :src="avatar || defaultAvatar" 
            class="user-avatar" 
            @click="goProfile"
          />
          <div class="dropdown">
            <div class="dropdown-item" @click="goProfile">我的主页</div>
            <div class="dropdown-item" @click="logout">退出登录</div>
          </div>
        </div>
        <router-link v-else to="/login" class="login-link">登录</router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const avatar = ref('')
const searchKeyword = ref('')
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

onMounted(() => {
  username.value = localStorage.getItem('username') || ''
  avatar.value = localStorage.getItem('avatar') || ''
  window.addEventListener('focus', updateAvatar)
})

const updateAvatar = () => {
  avatar.value = localStorage.getItem('avatar') || ''
}

onUnmounted(() => {
  window.removeEventListener('focus', updateAvatar)
})

const goProfile = () => {
  const userId = localStorage.getItem('userId')
  if (userId) router.push(`/profile/${userId}`)
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

const doSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push({ path: '/search', query: { q: keyword } })
  }
}
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.logo-img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.search-box {
  flex: 1;
  max-width: 400px;
  margin: 0 30px;
  position: relative;
}

.search-box input {
  width: 100%;
  padding: 8px 40px 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.search-box input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.search-box .search-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  font-size: 14px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #1890ff;
  color: #fff;
  border-radius: 20px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.message-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #1890ff;
  color: #fff;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.message-link:hover {
  background: #40a9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.4);
}

.message-link .icon {
  font-size: 14px;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.4);
}

.publish-btn .icon {
  font-size: 14px;
}

.user-menu {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.user-avatar:hover {
  border-color: #1890ff;
  transform: scale(1.05);
}

.dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 10px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
}

.user-menu:hover .dropdown {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-item {
  padding: 12px 20px;
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  transition: background 0.2s ease;
}

.dropdown-item:first-child {
  border-radius: 12px 12px 0 0;
}

.dropdown-item:last-child {
  border-radius: 0 0 12px 12px;
}

.dropdown-item:hover {
  background: #f5f5f5;
}

.login-link {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .message-link {
    display: none;
  }
  
  .publish-btn {
    padding: 6px 12px;
  }
  
  .publish-btn .icon {
    font-size: 12px;
  }
  
  .publish-btn span:not(.icon) {
    display: none;
  }
}
</style>