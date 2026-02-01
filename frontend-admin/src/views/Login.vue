<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧装饰 -->
      <div class="login-decoration">
        <div class="decoration-content">
          <div class="logo">
            <div class="logo-icon">
              <el-icon><Edit /></el-icon>
            </div>
            <span>BlogSpace</span>
          </div>
          <h1>欢迎回来</h1>
          <p>登录您的账户，继续您的创作之旅</p>
          <div class="features">
            <div class="feature-item">
              <el-icon><EditPen /></el-icon>
              <span>自由创作，分享思想</span>
            </div>
            <div class="feature-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>与读者互动交流</span>
            </div>
            <div class="feature-item">
              <el-icon><TrendCharts /></el-icon>
              <span>追踪文章数据</span>
            </div>
          </div>
        </div>
        <div class="decoration-shapes">
          <div class="shape s1"></div>
          <div class="shape s2"></div>
          <div class="shape s3"></div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-wrapper">
        <div class="form-header">
          <h2>登录</h2>
          <p>输入您的账户信息</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <div class="input-wrapper">
              <el-icon class="input-icon"><User /></el-icon>
              <el-input 
                v-model="form.username" 
                placeholder="用户名"
                size="large"
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="password">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="密码"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          </div>

          <el-button 
            type="primary" 
            size="large" 
            class="submit-btn"
            :loading="loading" 
            @click="handleLogin"
          >
            <span v-if="!loading">登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form>

        <div class="form-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage({
      message: `欢迎回来，${form.username}！🎉`,
      type: 'success',
      duration: 3000
    })
    router.push('/home')
  } catch (e) {
    ElMessage({
      message: '登录失败，请检查用户名和密码',
      type: 'error',
      duration: 3000
    })
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-color);
  padding: 20px;
}

.login-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

// 左侧装饰
.login-decoration {
  flex: 1;
  background: var(--primary-gradient);
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
  
  .decoration-content {
    position: relative;
    z-index: 1;
    color: white;
    
    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 40px;
      
      .logo-icon {
        width: 48px;
        height: 48px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
      }
      
      span {
        font-size: 24px;
        font-weight: 700;
      }
    }
    
    h1 {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: 16px;
    }
    
    p {
      font-size: 16px;
      opacity: 0.9;
      margin-bottom: 40px;
    }
    
    .features {
      display: flex;
      flex-direction: column;
      gap: 20px;
      
      .feature-item {
        display: flex;
        align-items: center;
        gap: 14px;
        font-size: 15px;
        
        .el-icon {
          width: 40px;
          height: 40px;
          background: rgba(255, 255, 255, 0.15);
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
        }
      }
    }
  }
  
  .decoration-shapes {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    
    .shape {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.08);
      
      &.s1 {
        width: 300px;
        height: 300px;
        top: -100px;
        right: -100px;
      }
      
      &.s2 {
        width: 200px;
        height: 200px;
        bottom: -50px;
        left: -50px;
      }
      
      &.s3 {
        width: 100px;
        height: 100px;
        top: 50%;
        left: 30%;
      }
    }
  }
}

// 右侧表单
.login-form-wrapper {
  flex: 1;
  padding: 60px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  
  .form-header {
    margin-bottom: 40px;
    
    h2 {
      font-size: 28px;
      font-weight: 700;
      color: var(--text-primary);
      margin-bottom: 8px;
    }
    
    p {
      color: var(--text-secondary);
      font-size: 15px;
    }
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
  
  .input-wrapper {
    position: relative;
    width: 100%;
    
    .input-icon {
      position: absolute;
      left: 16px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 18px;
      color: var(--text-secondary);
      z-index: 1;
    }
    
    :deep(.el-input__wrapper) {
      padding-left: 48px;
      height: 52px;
      border-radius: var(--radius-md);
      
      .el-input__inner {
        font-size: 15px;
      }
    }
  }
  
  .form-options {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-bottom: 28px;
  }
  
  .submit-btn {
    width: 100%;
    height: 52px;
    font-size: 16px;
    font-weight: 600;
    border-radius: var(--radius-md);
  }
}

.form-footer {
  text-align: center;
  margin-top: 32px;
  font-size: 15px;
  color: var(--text-secondary);
  
  .register-link {
    color: var(--primary-color);
    font-weight: 600;
    margin-left: 8px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    max-width: 420px;
  }
  
  .login-decoration {
    padding: 32px;
    min-height: auto;
    
    h1 {
      font-size: 28px;
    }
    
    .features {
      display: none;
    }
  }
  
  .login-form-wrapper {
    padding: 32px 24px;
  }
}
</style>
