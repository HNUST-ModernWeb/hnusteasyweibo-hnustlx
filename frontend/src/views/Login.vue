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
          <button :class="['tab-btn', { active: isLogin }]" @click="isLogin = true">登录</button>
          <button :class="['tab-btn', { active: !isLogin }]" @click="isLogin = false">注册</button>
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
            <span class="eye-icon" @click="showPassword = !showPassword">{{ showPassword ? '🙈' : '👁️' }}</span>
          </div>

          <label class="remember-row">
            <input v-model="rememberAccount" type="checkbox" />
            <span>记住账号</span>
          </label>

          <button type="submit" class="submit-btn" :class="{ loading }" :disabled="loading">
            <span v-if="loading" class="btn-spinner"></span>
            {{ loading ? '请稍候...' : (isLogin ? '登录' : '注册') }}
          </button>
          <p v-if="cooldownSeconds > 0 && isLogin" class="risk-tip">
            登录尝试过于频繁，请在 {{ cooldownSeconds }} 秒后重试
          </p>
        </form>

        <div class="login-footer">
          <p v-if="isLogin">还没有账号？<span @click="isLogin = false">立即注册</span></p>
          <p v-else>已有账号？<span @click="isLogin = true">立即登录</span></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api'
import { notify } from '../utils/notify'

const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)
const showPassword = ref(false)
const form = reactive({ username: '', password: '' })
const failedAttempts = ref(0)
const lockUntil = ref(0)
const rememberAccount = ref(false)

const usernamePattern = /^[a-zA-Z0-9_]{3,15}$/
const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d).{6,20}$/
const cooldownSeconds = computed(() => {
  const diff = Math.ceil((lockUntil.value - Date.now()) / 1000)
  return diff > 0 ? diff : 0
})

const submit = async () => {
  if (isLogin.value && cooldownSeconds.value > 0) {
    notify.error(`请在 ${cooldownSeconds.value} 秒后重试`)
    return
  }

  if (!form.username || !form.password) {
    notify.error('请填写完整信息')
    return
  }

  if (!usernamePattern.test(form.username)) {
    notify.error('用户名需为 3-15 位字母、数字或下划线')
    return
  }

  loading.value = true
  try {
    if (isLogin.value) {
      const res = await userApi.login(form)
      console.log('登录响应:', res)
      
      // 后端返回格式: { code: 200, data: { userId, username, token, avatar } }
      // axios返回完整response: res = { data: { code:200, data:{userId,token} } }
      // 需要res.data.data才能获取userId和token
      const userData = res?.data
      console.log('userData:', userData)
      console.log('token:', userData?.token, 'userId:', userData?.userId)
      
      if (userData?.token && userData?.userId) {
        localStorage.setItem('token', userData.token)
        localStorage.setItem('userId', String(userData.userId))
        localStorage.setItem('username', userData.username || '')
        localStorage.setItem('avatar', userData.avatar || '')
        notify.success('登录成功，欢迎回来')
        if (rememberAccount.value) {
          localStorage.setItem('remember_username', form.username)
        } else {
          localStorage.removeItem('remember_username')
        }
        failedAttempts.value = 0
        lockUntil.value = 0
        setTimeout(() => router.push('/'), 300)
        return
      } else {
        console.log('用户数据缺失:', userData)
        notify.error('登录失败，返回数据异常')
        return
      }
    } else {
      await userApi.register(form)
      notify.success('注册成功，请登录')
      isLogin.value = true
      form.password = ''
    }
  } catch (e) {
    const status = e?.response?.status
    const msg = e?.response?.data?.msg
    if (isLogin.value) {
      failedAttempts.value += 1
      if (status === 403) {
        notify.error(msg || '账号已被禁用，请联系管理员')
      } else if (status === 400) {
        notify.error(msg || '用户名或密码错误')
      } else if (status === 429) {
        notify.error('请求过于频繁，请稍后重试')
      } else {
        notify.error(msg || '登录失败，请稍后重试')
      }

      if (failedAttempts.value >= 5) {
        lockUntil.value = Date.now() + 30 * 1000
        failedAttempts.value = 0
        notify.info('为保障账号安全，已临时限制登录 30 秒')
      }
    } else {
      notify.error(msg || '注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const remembered = localStorage.getItem('remember_username')
  if (remembered) {
    form.username = remembered
    rememberAccount.value = true
  }
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4ff 0%, #f0f7ff 100%);
  position: relative;
  overflow: hidden;
}

.login-page::before,
.login-page::after {
  content: '';
  position: absolute;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  filter: blur(2px);
  opacity: 0.55;
  animation: float 8s ease-in-out infinite;
}

.login-page::before {
  top: -80px;
  left: -70px;
  background: radial-gradient(circle at 30% 30%, #60a5fa, #93c5fd);
}

.login-page::after {
  right: -90px;
  bottom: -100px;
  background: radial-gradient(circle at 30% 30%, #34d399, #6ee7b7);
  animation-delay: 1.6s;
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: 40px 35px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.7);
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

.submit-btn.loading {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.45);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.risk-tip {
  margin: 8px 0 0;
  font-size: 13px;
  color: #d97706;
  text-align: center;
}

.remember-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--text-light);
  font-size: 13px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-14px);
  }
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
