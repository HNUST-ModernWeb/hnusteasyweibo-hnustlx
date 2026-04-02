<template>
  <div class="users-page">
    <div class="toolbar">
      <div class="search-box">
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="搜索用户名..." 
          @input="handleSearch"
        />
      </div>
      <div class="toolbar-right">
        <span class="total">共 {{ total }} 用户</span>
      </div>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>头像</th>
            <th>用户名</th>
            <th>注册时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.userId">
            <td>{{ user.userId }}</td>
            <td>
              <img :src="user.avatar || defaultAvatar" class="user-avatar" />
            </td>
            <td>{{ user.username }}</td>
            <td>{{ formatTime(user.createTime) }}</td>
            <td>
              <span :class="['status-badge', user.status === 0 ? 'disabled' : 'active']">
                {{ user.status === 0 ? '禁用' : '正常' }}
              </span>
            </td>
            <td>
              <div class="action-buttons">
                <button 
                  v-if="user.status === 1"
                  class="btn btn-warning"
                  @click="toggleStatus(user)"
                >
                  禁用
                </button>
                <button 
                  v-else
                  class="btn btn-success"
                  @click="toggleStatus(user)"
                >
                  启用
                </button>
                <button 
                  class="btn btn-danger"
                  @click="deleteUser(user)"
                >
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-if="!loading && users.length === 0" class="empty-state">
        <p>暂无用户数据</p>
      </div>
    </div>
    
    <div v-if="total > pageSize" class="pagination">
      <button 
        class="page-btn" 
        :disabled="page <= 1"
        @click="changePage(page - 1)"
      >
        上一页
      </button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button 
        class="page-btn" 
        :disabled="page >= totalPages"
        @click="changePage(page + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../api'

const users = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await adminApi.listUsers({ 
      page: page.value, 
      pageSize: pageSize.value 
    })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('获取用户列表失败:', e)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

const handleSearch = () => {
  page.value = 1
  fetchUsers()
}

const toggleStatus = async (user) => {
  const action = user.status === 0 ? '启用' : '禁用'
  if (!confirm(`确定要${action}该用户吗？`)) return
  
  try {
    await adminApi.updateUserStatus({
      userId: user.userId,
      isDeleted: user.status === 0 ? 0 : 1
    })
    alert(`${action}成功`)
    fetchUsers()
  } catch (e) {
    console.error('操作失败:', e)
    alert('操作失败: ' + (e.response?.data?.msg || e.message))
  }
}

const deleteUser = async (user) => {
  if (!confirm(`确定要删除用户 "${user.username}" 吗？此操作不可恢复！`)) return
  
  try {
    await adminApi.deleteUser(user.userId)
    alert('删除成功')
    fetchUsers()
  } catch (e) {
    console.error('删除失败:', e)
    alert('删除失败: ' + (e.response?.data?.msg || e.message))
  }
}

const changePage = (newPage) => {
  page.value = newPage
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-page {
  animation: fadeIn 0.3s ease;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-box input {
  width: 250px;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.search-box input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

.total {
  color: #999;
  font-size: 14px;
}

.table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 16px 20px;
  text-align: left;
}

.data-table th {
  background: #fafafa;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}

.data-table td {
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #f5f5f5;
}

.data-table tr:hover {
  background: #fafafa;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.disabled {
  background: #fff2f0;
  color: #ff4d4f;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-warning {
  background: #faad14;
  color: #fff;
}

.btn-warning:hover {
  background: #d48806;
}

.btn-success {
  background: #52c41a;
  color: #fff;
}

.btn-success:hover {
  background: #389e0d;
}

.btn-danger {
  background: #ff4d4f;
  color: #fff;
}

.btn-danger:hover {
  background: #cf1322;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  margin-top: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
