<template>
  <div class="tags-page">
    <div class="toolbar">
      <div class="add-form">
        <input 
          v-model="newTagName" 
          type="text" 
          placeholder="输入标签名称" 
          maxlength="30"
          @keyup.enter="addTag"
        />
        <button class="add-btn" @click="addTag" :disabled="!newTagName.trim() || adding">
          {{ adding ? '添加中...' : '+ 添加标签' }}
        </button>
      </div>
      <div class="toolbar-right">
        <span class="total">共 {{ total }} 个标签</span>
      </div>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标签名称</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tag in tags" :key="tag.tagId">
            <td>{{ tag.tagId }}</td>
            <td>
              <span class="tag-name">{{ tag.tagName }}</span>
            </td>
            <td>{{ formatTime(tag.createTime) }}</td>
            <td>
              <button class="btn btn-danger" @click="deleteTag(tag)">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-if="!loading && tags.length === 0" class="empty-state">
        <p>暂无标签，请添加</p>
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

const tags = ref([])
const loading = ref(false)
const adding = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const newTagName = ref('')

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const fetchTags = async () => {
  loading.value = true
  try {
    const res = await adminApi.listTags({ 
      page: page.value, 
      pageSize: pageSize.value 
    })
    tags.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('获取标签列表失败:', e)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

const addTag = async () => {
  const name = newTagName.value.trim()
  if (!name) return
  
  adding.value = true
  try {
    await adminApi.addTag({ tagName: name })
    alert('添加成功')
    newTagName.value = ''
    fetchTags()
  } catch (e) {
    console.error('添加标签失败:', e)
    alert('添加失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    adding.value = false
  }
}

const deleteTag = async (tag) => {
  if (!confirm(`确定要删除标签 "${tag.tagName}" 吗？`)) return
  
  try {
    await adminApi.deleteTag({ tagId: tag.tagId })
    alert('删除成功')
    fetchTags()
  } catch (e) {
    console.error('删除标签失败:', e)
    alert('删除失败: ' + (e.response?.data?.msg || e.message))
  }
}

const changePage = (newPage) => {
  page.value = newPage
  fetchTags()
}

onMounted(() => {
  fetchTags()
})
</script>

<style scoped>
.tags-page {
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

.add-form {
  display: flex;
  gap: 12px;
}

.add-form input {
  width: 250px;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.add-form input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

.add-btn {
  padding: 10px 20px;
  background: #1890ff;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-btn:hover:not(:disabled) {
  background: #40a9ff;
  transform: translateY(-2px);
}

.add-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.tag-name {
  padding: 4px 12px;
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
  border-radius: 4px;
  font-size: 13px;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
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
