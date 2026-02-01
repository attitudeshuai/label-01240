<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="header-left">
          <router-link to="/home" class="logo">
            <div class="logo-icon">
              <el-icon><Edit /></el-icon>
            </div>
            <span class="logo-text">BlogSpace</span>
          </router-link>
        </div>
        
        <nav class="header-nav">
          <router-link to="/home" class="nav-link" :class="{ active: isActive('/home') }">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link v-if="isLoggedIn" to="/write" class="nav-link" :class="{ active: isActive('/write') }">
            <el-icon><EditPen /></el-icon>
            <span>写文章</span>
          </router-link>
          <router-link v-if="isLoggedIn" to="/my-blogs" class="nav-link" :class="{ active: isActive('/my-blogs') }">
            <el-icon><Document /></el-icon>
            <span>我的文章</span>
          </router-link>
          <el-dropdown v-if="isAdmin" trigger="hover" class="admin-dropdown">
            <span class="nav-link" :class="{ active: isAdminRoute }">
              <el-icon><Setting /></el-icon>
              <span>管理</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="admin-menu">
                <el-dropdown-item @click="$router.push('/admin/users')">
                  <el-icon><User /></el-icon>用户管理
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/admin/blogs')">
                  <el-icon><Document /></el-icon>内容管理
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/admin/comments')">
                  <el-icon><ChatDotRound /></el-icon>评论管理
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>

        <div class="header-right">
          <template v-if="isLoggedIn">
            <el-dropdown trigger="hover" @command="handleCommand">
              <div class="user-dropdown">
                <el-avatar :size="36" :src="userInfo?.avatar || ''" class="user-avatar">
                  {{ userInfo?.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <div class="user-meta">
                  <span class="user-name">{{ userInfo?.username }}</span>
                  <el-tag v-if="isAdmin" size="small" type="danger" effect="dark">管理员</el-tag>
                </div>
                <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-menu">
                  <div class="user-menu-header">
                    <el-avatar :size="48" :src="userInfo?.avatar || ''">
                      {{ userInfo?.username?.charAt(0)?.toUpperCase() }}
                    </el-avatar>
                    <div class="user-menu-info">
                      <span class="name">{{ userInfo?.username }}</span>
                      <span class="email">{{ userInfo?.email || '未设置邮箱' }}</span>
                    </div>
                  </div>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="my-blogs">
                    <el-icon><Document /></el-icon>我的文章
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button class="login-btn" @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" class="register-btn" @click="$router.push('/register')">
              免费注册
            </el-button>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main">
      <transition name="fade" mode="out-in">
        <router-view />
      </transition>
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      <div class="footer-content">
        <div class="footer-brand">
          <div class="logo-icon small">
            <el-icon><Edit /></el-icon>
          </div>
          <span>BlogSpace</span>
        </div>
        <p class="copyright">© 2024 BlogSpace · 用文字记录生活</p>
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">使用条款</a>
          <a href="#">隐私政策</a>
        </div>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin())
const isAdminRoute = computed(() => route.path.startsWith('/admin'))

function isActive(path) {
  return route.path === path
}

function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'my-blogs') {
    router.push('/my-blogs')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage({
      message: '已安全退出，期待您的再次访问 👋',
      type: 'success',
      duration: 3000
    })
    router.push('/home')
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-color);
}

.header {
  height: 72px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

.header-left {
  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    text-decoration: none;
    
    .logo-icon {
      width: 40px;
      height: 40px;
      background: var(--primary-gradient);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      transition: transform var(--transition-normal);
      
      &:hover {
        transform: scale(1.05);
      }
    }
    
    .logo-text {
      font-size: 22px;
      font-weight: 700;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .nav-link {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 18px;
    border-radius: var(--radius-sm);
    color: var(--text-regular);
    text-decoration: none;
    font-weight: 500;
    font-size: 15px;
    transition: all var(--transition-normal);
    cursor: pointer;
    
    .el-icon {
      font-size: 18px;
    }
    
    .arrow {
      font-size: 14px;
      margin-left: 2px;
    }
    
    &:hover {
      color: var(--primary-color);
      background: rgba(92, 107, 192, 0.08);
    }
    
    &.active {
      color: var(--primary-color);
      background: rgba(92, 107, 192, 0.12);
    }
  }
}

.admin-dropdown {
  .admin-menu {
    min-width: 160px;
    
    .el-dropdown-item {
      padding: 12px 16px;
      
      .el-icon {
        margin-right: 8px;
      }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .login-btn {
    border-radius: var(--radius-sm);
    font-weight: 500;
  }
  
  .register-btn {
    border-radius: var(--radius-sm);
    font-weight: 500;
    padding: 10px 24px;
  }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  
  &:hover {
    background: var(--bg-secondary);
  }
  
  .user-avatar {
    border: 2px solid var(--primary-light);
  }
  
  .user-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .user-name {
      font-weight: 600;
      font-size: 14px;
      color: var(--text-primary);
    }
  }
  
  .dropdown-arrow {
    color: var(--text-secondary);
    font-size: 14px;
  }
}

.user-menu {
  min-width: 220px;
  padding: 8px;
  
  .user-menu-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border-bottom: 1px solid var(--border-light);
    margin-bottom: 8px;
    
    .user-menu-info {
      display: flex;
      flex-direction: column;
      
      .name {
        font-weight: 600;
        font-size: 15px;
        color: var(--text-primary);
      }
      
      .email {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
  }
  
  .el-dropdown-item {
    padding: 12px 16px;
    border-radius: var(--radius-sm);
    margin: 2px 0;
    
    .el-icon {
      margin-right: 10px;
      font-size: 16px;
    }
    
    &:hover {
      background: var(--bg-secondary);
    }
  }
}

.main {
  flex: 1;
  padding: 32px 24px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

.footer {
  background: var(--bg-card);
  border-top: 1px solid var(--border-light);
  padding: 32px 24px;
  height: auto;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  
  .footer-brand {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .logo-icon.small {
      width: 32px;
      height: 32px;
      background: var(--primary-gradient);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 16px;
    }
    
    span {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }
  
  .copyright {
    color: var(--text-secondary);
    font-size: 14px;
  }
  
  .footer-links {
    display: flex;
    gap: 24px;
    
    a {
      color: var(--text-secondary);
      font-size: 14px;
      transition: color var(--transition-fast);
      
      &:hover {
        color: var(--primary-color);
      }
    }
  }
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 响应式
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .header-nav {
    display: none;
  }
  
  .logo-text {
    display: none;
  }
  
  .user-meta {
    display: none !important;
  }
  
  .main {
    padding: 16px;
  }
}
</style>
