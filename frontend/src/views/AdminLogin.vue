<template>
  <div class="admin-login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <span class="logo-text">科大微博管理后台</span>
          <p class="slogan">管理员登录</p>
        </div>
        
        <form @submit.prevent="submit" class="login-form">
          <div class="input-group">
            <span class="input-icon">👤</span>
            <input 
              v-model="form.username" 
              type="text" 
              placeholder="请输入管理员账号" 
              required 
            />
          </div>
          
          <div class="input-group">
            <span class="input-icon">🔒</span>
            <input 
              v-model="form.password" 
              :type="showPassword ? 'text' : 'password'" 
              placeholder="请输入密码" 
              required 
            />
            <span class="eye-icon" @click="showPassword = !showPassword">
              {{ showPassword ? '👁️' : '👁️‍🗨️' }}
            </span>
          </div>
          
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>
        
        <div class="login-footer">
          <router-link to="/" class="back-link">返回首页</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../api'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const form = reactive({ username: '', password: '' })

const submit = async () => {
  if (!form.username || !form.password) {
    alert('请填写完整信息')
    return
  }
  
  loading.value = true
  try {
    const res = await adminApi.login(form)
    console.log('管理员登录返回:', res)
    
    const data = res.data
    if (data && data.token) {
      localStorage.setItem('admin_token', data.token)
      localStorage.setItem('admin_username', data.username || form.username)
      router.push('/admin/dashboard')
    } else {
      alert('登录失败，请重试')
    }
  } catch (e) {
    console.error(e)
    alert('登录失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.login-container {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 35px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  display: block;
  margin-bottom: 8px;
}

.slogan {
  color: #666;
  font-size: 14px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 12px;
  padding: 0 15px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.input-group:focus-within {
  border-color: #1890ff;
  background: #fff;
}

.input-icon {
  font-size: 18px;
  margin-right: 10px;
}

.input-group input {
  flex: 1;
  padding: 14px 0;
  border: none;
  background: transparent;
  font-size: 15px;
  outline: none;
  color: #333;
}

.input-group input::placeholder {
  color: #aaa;
}

.eye-icon {
  cursor: pointer;
  font-size: 16px;
  opacity: 0.6;
}

.eye-icon:hover {
  opacity: 1;
}

.submit-btn {
  margin-top: 10px;
  padding: 15px;
  border: none;
  border-radius: 12px;
  background: #1890ff;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(24, 144, 255, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-footer {
  margin-top: 25px;
  text-align: center;
}

.back-link {
  color: #1890ff;
  font-size: 14px;
  text-decoration: none;
}

.back-link:hover {
  text-decoration: underline;
}
</style>
