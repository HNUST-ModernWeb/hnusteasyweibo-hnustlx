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
            <input v-model="form.username" type="text" placeholder="请输入管理员账号" required />
          </div>

          <div class="input-group">
            <span class="input-icon">🔒</span>
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
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
            {{ loading ? '登录中...' : '登录' }}
          </button>
          <p v-if="cooldownSeconds > 0" class="risk-tip">
            登录尝试过于频繁，请在 {{ cooldownSeconds }} 秒后重试
          </p>
        </form>

        <div class="login-footer">
          <router-link to="/" class="back-link">返回首页</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../api'
import { notify } from '../utils/notify'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const form = reactive({ username: '', password: '' })
const failedAttempts = ref(0)
const lockUntil = ref(0)
const rememberAccount = ref(false)
const cooldownSeconds = computed(() => {
  const diff = Math.ceil((lockUntil.value - Date.now()) / 1000)
  return diff > 0 ? diff : 0
})

const submit = async () => {
  if (cooldownSeconds.value > 0) {
    notify.error(`请在 ${cooldownSeconds.value} 秒后重试`)
    return
  }

  if (!form.username || !form.password) {
    notify.error('请填写完整信息')
    return
  }

  if (form.username.length < 3) {
    notify.error('管理员账号长度至少 3 位')
    return
  }

  loading.value = true
  try {
    const res = await adminApi.login(form)
    const data = res?.data

    if (data && data.token) {
      localStorage.setItem('admin_token', data.token)
      localStorage.setItem('admin_username', data.username || form.username)
      if (rememberAccount.value) {
        localStorage.setItem('remember_admin_username', form.username)
      } else {
        localStorage.removeItem('remember_admin_username')
      }
      notify.success('登录成功，正在进入后台')
      failedAttempts.value = 0
      lockUntil.value = 0
      setTimeout(() => router.push('/admin/dashboard'), 300)
    } else {
      notify.error('登录失败，请重试')
    }
  } catch (e) {
    failedAttempts.value += 1
    const status = e?.response?.status
    const msg = e?.response?.data?.msg
    if (status === 403) {
      notify.error(msg || '无管理员权限')
    } else if (status === 400) {
      notify.error(msg || '账号或密码错误')
    } else if (status === 429) {
      notify.error('请求过于频繁，请稍后重试')
    } else {
      notify.error(msg || e.message || '登录失败，请稍后重试')
    }

    if (failedAttempts.value >= 5) {
      lockUntil.value = Date.now() + 30 * 1000
      failedAttempts.value = 0
      notify.info('为保障账号安全，已临时限制登录 30 秒')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const remembered = localStorage.getItem('remember_admin_username')
  if (remembered) {
    form.username = remembered
    rememberAccount.value = true
  }
})
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  position: relative;
  overflow: hidden;
}

.admin-login-page::before,
.admin-login-page::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  opacity: 0.42;
  filter: blur(1px);
  animation: float 8s ease-in-out infinite;
}

.admin-login-page::before {
  top: -80px;
  left: -80px;
  background: radial-gradient(circle at 30% 30%, #3b82f6, #2563eb);
}

.admin-login-page::after {
  right: -90px;
  bottom: -100px;
  background: radial-gradient(circle at 30% 30%, #22c55e, #16a34a);
  animation-delay: 1.8s;
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 16px;
  padding: 40px 35px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.5);
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
  color: #6b7280;
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
