<template>
  <div class="home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1>探索精彩内容</h1>
        <p>发现优质文章，与创作者交流思想</p>
      </div>
      <div class="banner-decoration">
        <div class="circle c1"></div>
        <div class="circle c2"></div>
        <div class="circle c3"></div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-wrapper">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="搜索文章标题、标签或作者..."
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" class="search-btn" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 博客列表 -->
    <div class="blog-section">
      <div class="section-header">
        <h2>
          <el-icon><Document /></el-icon>
          最新文章
        </h2>
        <span class="article-count">共 {{ total }} 篇</span>
      </div>

      <div v-loading="loading" class="blog-list">
        <template v-if="blogs.length > 0">
          <article 
            v-for="(blog, index) in blogs" 
            :key="blog.id" 
            class="blog-card"
            :style="{ animationDelay: `${index * 0.1}s` }"
            @click="goDetail(blog.id)"
          >
            <div class="card-header">
              <div class="author-info" @click.stop="goUserProfile(blog.authorId)">
                <el-avatar :size="44" :src="blog.authorAvatar || ''" class="author-avatar">
                  {{ blog.authorName?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <div class="author-meta">
                  <span class="author-name">{{ blog.authorName }}</span>
                  <span class="publish-time">
                    <el-icon><Clock /></el-icon>
                    {{ formatTime(blog.createdAt) }}
                  </span>
                </div>
              </div>
            </div>
            
            <div class="card-body">
              <h3 class="blog-title">{{ blog.title }}</h3>
              <p class="blog-summary">{{ getSummary(blog.content) }}</p>
            </div>
            
            <div class="card-footer">
              <div class="tags" v-if="blog.tags">
                <el-tag 
                  v-for="tag in blog.tags.split(',').slice(0, 3)" 
                  :key="tag" 
                  size="small"
                  effect="plain"
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <div class="stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ formatNumber(blog.viewCount) }}
                </span>
                <span class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ formatNumber(blog.commentCount) }}
                </span>
                <span class="stat-item like" :class="{ liked: blog.liked }">
                  <el-icon><Star /></el-icon>
                  {{ formatNumber(blog.likeCount) }}
                </span>
              </div>
            </div>
          </article>
        </template>
        
        <div v-else-if="!loading" class="empty-state">
          <el-icon><Document /></el-icon>
          <p>暂无文章</p>
          <el-button type="primary" @click="$router.push('/write')">发布第一篇文章</el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > size">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @change="fetchBlogs"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { blogApi } from '../api'

const router = useRouter()

const loading = ref(false)
const blogs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

onMounted(() => {
  fetchBlogs()
})

async function fetchBlogs() {
  loading.value = true
  try {
    const res = await blogApi.getList({
      page: page.value,
      size: size.value,
      keyword: keyword.value
    })
    blogs.value = res.data.list
    total.value = res.data.total
  } catch (e) {
    ElMessage.error('加载文章失败，请稍后重试')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchBlogs()
  if (keyword.value) {
    ElMessage({
      message: `正在搜索 "${keyword.value}"`,
      type: 'info',
      duration: 2000
    })
  }
}

function goDetail(id) {
  router.push(`/blog/${id}`)
}

function goUserProfile(userId) {
  router.push(`/user/${userId}`)
}

function getSummary(content) {
  const text = content.replace(/<[^>]+>/g, '')
  return text.length > 150 ? text.substring(0, 150) + '...' : text
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

function formatNumber(num) {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num || 0
}
</script>

<style lang="scss" scoped>
.home {
  max-width: 900px;
  margin: 0 auto;
}

// 欢迎横幅
.welcome-banner {
  position: relative;
  background: var(--primary-gradient);
  border-radius: var(--radius-xl);
  padding: 48px 40px;
  margin-bottom: 32px;
  overflow: hidden;
  
  .banner-content {
    position: relative;
    z-index: 1;
    
    h1 {
      font-size: 32px;
      font-weight: 700;
      color: white;
      margin-bottom: 12px;
    }
    
    p {
      font-size: 16px;
      color: rgba(255, 255, 255, 0.85);
    }
  }
  
  .banner-decoration {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    width: 50%;
    
    .circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      
      &.c1 {
        width: 200px;
        height: 200px;
        top: -50px;
        right: -50px;
      }
      
      &.c2 {
        width: 150px;
        height: 150px;
        bottom: -30px;
        right: 100px;
      }
      
      &.c3 {
        width: 80px;
        height: 80px;
        top: 50%;
        right: 30%;
      }
    }
  }
}

// 搜索区域
.search-section {
  margin-bottom: 32px;
}

.search-wrapper {
  display: flex;
  align-items: center;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 8px 8px 8px 20px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
  
  &:focus-within {
    box-shadow: var(--shadow-lg);
    border-color: var(--primary-light);
  }
  
  .search-icon {
    font-size: 20px;
    color: var(--text-secondary);
    margin-right: 12px;
  }
  
  .search-input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 16px;
    color: var(--text-primary);
    background: transparent;
    
    &::placeholder {
      color: var(--text-placeholder);
    }
  }
  
  .search-btn {
    padding: 12px 28px;
    border-radius: var(--radius-md);
    font-size: 15px;
    
    .el-icon {
      margin-right: 6px;
    }
  }
}

// 博客区域
.blog-section {
  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
    
    h2 {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 22px;
      font-weight: 600;
      color: var(--text-primary);
      
      .el-icon {
        color: var(--primary-color);
      }
    }
    
    .article-count {
      font-size: 14px;
      color: var(--text-secondary);
      background: var(--bg-secondary);
      padding: 6px 14px;
      border-radius: 20px;
    }
  }
}

.blog-list {
  min-height: 400px;
}

// 博客卡片
.blog-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 28px;
  margin-bottom: 20px;
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-normal);
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-hover);
    border-color: var(--primary-light);
    
    .blog-title {
      color: var(--primary-color);
    }
  }
}

.card-header {
  margin-bottom: 16px;
  
  .author-info {
    display: flex;
    align-items: center;
    gap: 14px;
    
    &:hover .author-name {
      color: var(--primary-color);
    }
  }
  
  .author-avatar {
    border: 2px solid var(--primary-light);
    transition: transform var(--transition-fast);
    
    &:hover {
      transform: scale(1.1);
    }
  }
  
  .author-meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .author-name {
      font-weight: 600;
      font-size: 15px;
      color: var(--text-primary);
      transition: color var(--transition-fast);
    }
    
    .publish-time {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: var(--text-secondary);
      
      .el-icon {
        font-size: 14px;
      }
    }
  }
}

.card-body {
  margin-bottom: 20px;
  
  .blog-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
    line-height: 1.5;
    margin-bottom: 12px;
    transition: color var(--transition-fast);
    word-wrap: break-word;
    overflow-wrap: break-word;
    word-break: break-word;
  }
  
  .blog-summary {
    font-size: 15px;
    color: var(--text-regular);
    line-height: 1.7;
    word-wrap: break-word;
    overflow-wrap: break-word;
    word-break: break-word;
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
  
  .tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    
    .tag-item {
      background: rgba(92, 107, 192, 0.08);
      border-color: transparent;
      color: var(--primary-color);
      font-weight: 500;
      
      &:hover {
        background: rgba(92, 107, 192, 0.15);
      }
    }
  }
  
  .stats {
    display: flex;
    gap: 20px;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 5px;
      font-size: 14px;
      color: var(--text-secondary);
      transition: color var(--transition-fast);
      
      .el-icon {
        font-size: 16px;
      }
      
      &:hover {
        color: var(--primary-color);
      }
      
      &.like.liked {
        color: #F56C6C;
      }
    }
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 80px 20px;
  
  .el-icon {
    font-size: 72px;
    color: var(--text-placeholder);
    margin-bottom: 20px;
  }
  
  p {
    font-size: 18px;
    color: var(--text-secondary);
    margin-bottom: 24px;
  }
}

// 分页
.pagination {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
  
  :deep(.el-pagination) {
    justify-content: center;
    
    .el-pagination__total,
    .el-pagination__sizes,
    .el-pagination__jump {
      color: var(--text-secondary);
    }
  }
}

// 动画
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式
@media (max-width: 768px) {
  .welcome-banner {
    padding: 32px 24px;
    
    h1 {
      font-size: 24px;
    }
    
    .banner-decoration {
      display: none;
    }
  }
  
  .search-wrapper {
    flex-wrap: wrap;
    padding: 12px;
    
    .search-input {
      width: 100%;
      margin-bottom: 12px;
    }
    
    .search-btn {
      width: 100%;
    }
  }
  
  .blog-card {
    padding: 20px;
  }
  
  .card-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
