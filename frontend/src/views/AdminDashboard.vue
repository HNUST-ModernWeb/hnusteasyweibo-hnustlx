<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6f7ff;">👥</div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.userCount }}</span>
          <span class="stat-label">用户总数</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: #f6ffed;">📝</div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.postCount }}</span>
          <span class="stat-label">动态总数</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: #fff7e6;">💬</div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.commentCount }}</span>
          <span class="stat-label">评论总数</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: #f9f0ff;">🏷️</div>
        <div class="stat-content">
          <span class="stat-value">{{ stats.tagCount }}</span>
          <span class="stat-label">标签总数</span>
        </div>
      </div>
    </div>
    
    <div class="info-cards">
      <div class="info-card">
        <h3>系统信息</h3>
        <div class="info-item">
          <span class="info-label">系统名称</span>
          <span class="info-value">科大微博</span>
        </div>
        <div class="info-item">
          <span class="info-label">版本</span>
          <span class="info-value">1.0.0</span>
        </div>
        <div class="info-item">
          <span class="info-label">当前时间</span>
          <span class="info-value">{{ currentTime }}</span>
        </div>
      </div>
      
      <div class="info-card">
        <h3>快速操作</h3>
        <div class="quick-actions">
          <router-link to="/admin/users" class="action-btn">
            👥 管理用户
          </router-link>
          <router-link to="/admin/tags" class="action-btn">
            🏷️ 管理标签
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api'

const stats = ref({
  userCount: 0,
  postCount: 0,
  commentCount: 0,
  tagCount: 0
})

const currentTime = ref('')

const fetchStats = async () => {
  try {
    const res = await adminApi.getStats()
    if (res.data) {
      stats.value = {
        userCount: res.data.userCount || 0,
        postCount: res.data.postCount || 0,
        commentCount: res.data.commentCount || 0,
        tagCount: res.data.tagCount || 0
      }
    }
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  fetchStats()
  updateTime()
  setInterval(updateTime, 1000)
})
</script>

<style scoped>
.dashboard {
  animation: fadeIn 0.3s ease;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.info-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
}

.info-label {
  color: #999;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 12px 20px;
  background: #f5f5f5;
  border-radius: 8px;
  color: #333;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #1890ff;
  color: #fff;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
