<template>
  <router-view />
  <BottomNav />
</template>

<script setup>
import { onMounted } from 'vue'
import BottomNav from './components/BottomNav.vue'
import { wsService } from './utils/websocket'

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    wsService.connect()
  }
})
</script>

<style>
:root {
  --primary: #1890ff;
  --bg: #f5f5f5;
  --bg-card: #ffffff;
  --text: #333333;
  --text-secondary: #666666;
  --text-light: #999999;
  --border: #e8e8e8;
  --shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  --radius: 8px;
  --radius-full: 999px;
  --transition: all 0.3s ease;
  --bottom-nav-height: 50px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  color: var(--text);
  background: var(--bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  text-decoration: none;
  color: inherit;
}

button {
  border: none;
  background: none;
  cursor: pointer;
  font-size: inherit;
}

input, textarea {
  font-family: inherit;
  font-size: inherit;
}

.main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg);
  color: var(--text);
  font-size: 18px;
  transition: var(--transition);
}

.back-btn:hover {
  background: var(--border);
}

.loading-state {
  text-align: center;
  padding: 40px 0;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-light);
}

.empty-state .empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .main-container {
    padding: 12px;
  }
}
</style>
