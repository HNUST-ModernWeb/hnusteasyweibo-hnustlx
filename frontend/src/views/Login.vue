<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <img src="/logo.png" class="logo-img" alt="logo" />
            <span class="logo-text">科大微博</span>
          </div>
          <p class="slogan">校园动态，即时分享</p>
        </div>
        
        <div class="tab-switch">
          <button 
            :class="['tab-btn', { active: isLogin }]" 
            @click="isLogin = true"
          >
            登录
          </button>
          <button 
            :class="['tab-btn', { active: !isLogin }]" 
            @click="isLogin = false"
          >
            注册
          </button>
        </div>
        
        <form @submit.prevent="submit" class="login-form">
          <div class="input-group">
            <span class="input-icon">👤</span>
            <input 
              v-model="form.username" 
              type="text" 
              placeholder="请输入用户名" 
              maxlength="15"
              required 
            />
          </div>
          
          <div class="input-group">
            <span class="input-icon">🔒</span>
            <input 
              v-model="form.password" 
              :type="showPassword ? 'text' : 'password'" 
              placeholder="请输入密码" 
              maxlength="20"
              required 
            />
            <span class="eye-icon" @click="showPassword = !showPassword">
              {{ showPassword ? '👁️' : '👁️‍🗨️' }}
            </span>
          </div>
          
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '请稍候...' : (isLogin ? '登 录' : '注 册') }}
          </button>
        </form>
        
        <div class="login-footer">
          <p v-if="isLogin">
            还没有账号？<span @click="isLogin = false">立即注册</span>
          </p>
          <p v-else>
            已有账号？<span @click="isLogin = true">立即登录</span>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api'

const router = useRouter()
const isLogin = ref(true)
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
    if (isLogin.value) {
      const res = await userApi.login(form)
      console.log('登录返回:', res.data)
      const data = res.data
      
      // 兼容不同返回格式
      if (data.user) {
        localStorage.setItem('token', data.token)
        localStorage.setItem('userId', String(data.user.userId))
        localStorage.setItem('username', data.user.username)
        localStorage.setItem('avatar', data.user.avatar || '')
      } else if (data.token) {
        localStorage.setItem('token', data.token)
        localStorage.setItem('userId', String(data.userId))
        localStorage.setItem('username', data.username || '')
        localStorage.setItem('avatar', data.avatar || '')
      } else {
        console.error('登录返回数据格式异常:', data)
        alert('登录失败，请重试')
        return
      }
      router.push('/')
    } else {
      await userApi.register(form)
      alert('注册成功，请登录')
      isLogin.value = true
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4ff 0%, #f0f7ff 100%);
  position: relative;
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 40px 35px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}

.logo-img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.logo-icon {
  font-size: 36px;
}

.logo-text {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
}

.slogan {
  color: var(--text-light);
  font-size: 14px;
}

.tab-switch {
  display: flex;
  background: var(--bg);
  border-radius: var(--radius-full);
  padding: 4px;
  margin-bottom: 30px;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  border: none;
  background: transparent;
  border-radius: var(--radius-full);
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition);
}

.tab-btn.active {
  background: var(--primary);
  color: #fff;
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.3);
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
  background: var(--bg);
  border-radius: var(--radius);
  padding: 0 15px;
  border: 2px solid transparent;
  transition: var(--transition);
}

.input-group:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
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
  color: var(--text);
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
  border-radius: var(--radius);
  background: var(--primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition);
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
  color: var(--text-light);
  font-size: 14px;
}

.login-footer span {
  color: var(--primary);
  cursor: pointer;
  font-weight: 500;
}

.login-footer span:hover {
  text-decoration: underline;
}
</style>