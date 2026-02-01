<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 左侧装饰 -->
      <div class="register-decoration">
        <div class="decoration-content">
          <div class="logo">
            <div class="logo-icon">
              <el-icon><Edit /></el-icon>
            </div>
            <span>BlogSpace</span>
          </div>
          <h1>加入我们</h1>
          <p>创建账户，开启您的创作之旅</p>
          <div class="stats">
            <div class="stat-item">
              <span class="number">10K+</span>
              <span class="label">活跃作者</span>
            </div>
            <div class="stat-item">
              <span class="number">50K+</span>
              <span class="label">优质文章</span>
            </div>
            <div class="stat-item">
              <span class="number">100K+</span>
              <span class="label">读者用户</span>
            </div>
          </div>
        </div>
        <div class="decoration-shapes">
          <div class="shape s1"></div>
          <div class="shape s2"></div>
          <div class="shape s3"></div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="register-form-wrapper">
        <div class="form-header">
          <h2>创建账户</h2>
          <p>填写以下信息完成注册</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
          <el-form-item prop="username">
            <div class="input-wrapper">
              <el-icon class="input-icon"><User /></el-icon>
              <el-input 
                v-model="form.username" 
                placeholder="用户名 (3-20位字母数字下划线)"
                size="large"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="email">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Message /></el-icon>
              <el-input 
                v-model="form.email" 
                placeholder="邮箱 (选填)"
                size="large"
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="password">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="密码 (8-20位，包含字母和数字)"
                size="large"
                show-password
              />
            </div>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <div class="input-wrapper">
              <el-icon class="input-icon"><Lock /></el-icon>
              <el-input 
                v-model="form.confirmPassword" 
                type="password" 
                placeholder="确认密码"
                size="large"
                show-password
                @keyup.enter="handleRegister"
              />
            </div>
          </el-form-item>

          <el-button 
            type="primary" 
            size="large" 
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            <span v-if="!loading">创建账户</span>
            <span v-else>注册中...</span>
          </el-button>
        </el-form>

        <div class="form-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
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

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度在8-20之间', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      password: form.password,
      email: form.email
    })
    ElMessage({
      message: '注册成功！欢迎加入 BlogSpace 🎉',
      type: 'success',
      duration: 3000
    })
    router.push('/login')
  } catch (e) {
    ElMessage({
      message: '注册失败，请稍后重试',
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
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-color);
  padding: 20px;
}

.register-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 650px;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

// 左侧装饰
.register-decoration {
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
      margin-bottom: 48px;
    }
    
    .stats {
      display: flex;
      gap: 32px;
      
      .stat-item {
        text-align: center;
        
        .number {
          display: block;
          font-size: 28px;
          font-weight: 700;
          margin-bottom: 4px;
        }
        
        .label {
          font-size: 13px;
          opacity: 0.8;
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
.register-form-wrapper {
  flex: 1;
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  
  .form-header {
    margin-bottom: 32px;
    
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

.register-form {
  .el-form-item {
    margin-bottom: 20px;
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
      height: 50px;
      border-radius: var(--radius-md);
      
      .el-input__inner {
        font-size: 15px;
      }
    }
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
  margin-top: 28px;
  font-size: 15px;
  color: var(--text-secondary);
  
  .login-link {
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
  .register-container {
    flex-direction: column;
    max-width: 420px;
  }
  
  .register-decoration {
    padding: 32px;
    min-height: auto;
    
    h1 {
      font-size: 28px;
    }
    
    .stats {
      display: none;
    }
  }
  
  .register-form-wrapper {
    padding: 32px 24px;
  }
}
</style>
