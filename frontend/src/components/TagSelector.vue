<template>
  <div class="tag-selector">
    <div class="tag-header">
      <span class="tag-title">选择标签</span>
      <span class="tag-count">已选择: {{ selectedTags.length }}/{{ maxTags }}</span>
    </div>
    
    <div class="tag-list">
      <span 
        v-for="tag in tags" 
        :key="tag.tagId"
        :class="['tag-item', { active: isSelected(tag.tagId) }]"
        @click="toggleTag(tag)"
      >
        {{ tag.tagName }}
      </span>
    </div>
    
    <div v-if="loading" class="tag-loading">
      <div class="spinner"></div>
    </div>
    
    <div v-if="!loading && tags.length === 0" class="tag-empty">
      暂无标签
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { tagApi } from '../api'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  maxTags: {
    type: Number,
    default: 5
  }
})

const emit = defineEmits(['update:modelValue'])

const tags = ref([])
const loading = ref(false)
const selectedTags = ref([...props.modelValue])

const isSelected = (tagId) => {
  return selectedTags.value.some(t => t.tagId === tagId || t === tagId)
}

const toggleTag = (tag) => {
  const tagId = tag.tagId
  const idx = selectedTags.value.findIndex(t => t.tagId === tagId || t === tagId)
  
  if (idx > -1) {
    selectedTags.value.splice(idx, 1)
  } else {
    if (selectedTags.value.length < props.maxTags) {
      selectedTags.value.push(tag)
    } else {
      alert(`最多只能选择 ${props.maxTags} 个标签`)
    }
  }
  
  emit('update:modelValue', selectedTags.value)
}

const fetchTags = async () => {
  loading.value = true
  try {
    const res = await tagApi.list({ page: 1, pageSize: 50 })
    tags.value = res.data?.records || []
  } catch (e) {
    console.error('获取标签失败:', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (newVal) => {
  selectedTags.value = [...newVal]
})

onMounted(() => {
  fetchTags()
})
</script>

<style scoped>
.tag-selector {
  background: #fff;
  border-radius: var(--radius);
  padding: 16px;
  margin-bottom: 16px;
}

.tag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tag-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.tag-count {
  font-size: 12px;
  color: var(--text-light);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 6px 14px;
  font-size: 13px;
  color: var(--text-secondary);
  background: #f0f0f0;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: var(--transition);
}

.tag-item:hover {
  background: #e0e0e0;
  transform: scale(1.05);
}

.tag-item.active {
  background: var(--primary);
  color: #fff;
}

.tag-loading {
  text-align: center;
  padding: 20px;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #f0f0f0;
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.tag-empty {
  text-align: center;
  padding: 20px;
  color: var(--text-light);
  font-size: 14px;
}
</style>
