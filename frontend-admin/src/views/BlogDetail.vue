<template>
  <div class="blog-detail" v-loading="loading">
    <template v-if="blog">
      <!-- 返回按钮 -->
      <div class="back-nav">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>

      <!-- 博客内容 -->
      <article class="blog-article">
        <header class="article-header">
          <h1 class="article-title">{{ blog.title }}</h1>
          
          <div class="article-meta">
            <div class="author-info" @click="goUserProfile(blog.authorId)">
              <el-avatar :size="48" :src="blog.authorAvatar || ''" class="author-avatar">
                {{ blog.authorName?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <div class="author-details">
                <span class="author-name">{{ blog.authorName }}</span>
                <div class="publish-info">
                  <span><el-icon><Calendar /></el-icon> {{ formatTime(blog.createdAt) }}</span>
                  <span class="separator">·</span>
                  <span><el-icon><View /></el-icon> {{ blog.viewCount }} 阅读</span>
                </div>
              </div>
            </div>
          </div>

          <div class="article-tags" v-if="blog.tags">
            <el-tag 
              v-for="tag in blog.tags.split(',')" 
              :key="tag" 
              effect="plain"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
        </header>

        <div class="article-content blog-content" v-html="blog.content"></div>

        <footer class="article-footer">
          <div class="action-bar">
            <el-button 
              :class="['like-btn', { liked }]"
              size="large"
              round
              @click="handleLike"
            >
              <el-icon><Star /></el-icon>
              {{ liked ? '已点赞' : '点赞' }}
              <span class="count">{{ likeCount }}</span>
            </el-button>
            <el-button size="large" round @click="scrollToComments">
              <el-icon><ChatDotRound /></el-icon>
              评论
              <span class="count">{{ commentTotal }}</span>
            </el-button>
          </div>
        </footer>
      </article>

      <!-- 评论区 -->
      <section class="comments-section" id="comments">
        <div class="section-header">
          <h3>
            <el-icon><ChatDotRound /></el-icon>
            评论 ({{ commentTotal }})
          </h3>
        </div>
        
        <!-- 发表评论 -->
        <div class="comment-form" v-if="isLoggedIn">
          <el-avatar :size="40" :src="userStore.userInfo?.avatar || ''">
            {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div class="form-content">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
              maxlength="1000"
              show-word-limit
              resize="none"
            />
            <el-button 
              type="primary" 
              :loading="submitting" 
              :disabled="!commentContent.trim()"
              @click="submitComment"
            >
              发表评论
            </el-button>
          </div>
        </div>
        <div v-else class="login-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>请先 <router-link to="/login">登录</router-link> 后发表评论</span>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list" v-loading="commentsLoading">
          <template v-if="comments.length > 0">
            <div 
              v-for="(comment, index) in comments" 
              :key="comment.id" 
              class="comment-item"
              :style="{ animationDelay: `${index * 0.05}s` }"
            >
              <el-avatar :size="40" :src="comment.userAvatar || ''" class="comment-avatar">
                {{ comment.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="username">{{ comment.username }}</span>
                  <span class="time">{{ formatRelativeTime(comment.createdAt) }}</span>
                </div>
                <p class="comment-text">{{ comment.content }}</p>
                <div class="comment-actions">
                  <el-button 
                    v-if="canDeleteComment(comment)" 
                    type="danger" 
                    text 
                    size="small"
                    @click="deleteComment(comment.id)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="empty-comments">
            <el-icon><ChatDotRound /></el-icon>
            <p>暂无评论，来发表第一条评论吧</p>
          </div>
        </div>

        <!-- 评论分页 -->
        <div class="pagination" v-if="commentTotal > 10">
          <el-pagination
            v-model:current-page="commentPage"
            :total="commentTotal"
            :page-size="10"
            layout="prev, pager, next"
            background
            @change="fetchComments"
          />
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { blogApi, commentApi, likeApi } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const blogId = computed(() => route.params.id)
const isLoggedIn = computed(() => userStore.isLoggedIn)

const loading = ref(false)
const blog = ref(null)
const liked = ref(false)
const likeCount = ref(0)

const commentsLoading = ref(false)
const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentContent = ref('')
const submitting = ref(false)

onMounted(() => {
  fetchBlog()
  fetchComments()
  if (isLoggedIn.value) {
    fetchLikeStatus()
  }
})

async function fetchBlog() {
  loading.value = true
  try {
    const res = await blogApi.getDetail(blogId.value)
    blog.value = res.data
    likeCount.value = res.data.likeCount
  } catch (e) {
    ElMessage.error('加载文章失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  commentsLoading.value = true
  try {
    const res = await commentApi.getByBlogId(blogId.value, {
      page: commentPage.value,
      size: 10
    })
    comments.value = res.data.list
    commentTotal.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    commentsLoading.value = false
  }
}

async function fetchLikeStatus() {
  try {
    const res = await likeApi.getStatus(blogId.value)
    liked.value = res.data.liked
    likeCount.value = res.data.likeCount
  } catch (e) {
    console.error(e)
  }
}

async function handleLike() {
  if (!isLoggedIn.value) {
    ElMessage({
      message: '请先登录后再点赞',
      type: 'warning',
      duration: 2000
    })
    return
  }
  try {
    const res = await likeApi.toggle(blogId.value)
    liked.value = res.data.liked
    likeCount.value = res.data.likeCount
    ElMessage({
      message: liked.value ? '点赞成功 ❤️' : '已取消点赞',
      type: 'success',
      duration: 2000
    })
  } catch (e) {
    console.error(e)
  }
}

async function submitComment() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await commentApi.create({
      blogId: Number(blogId.value),
      content: commentContent.value
    })
    ElMessage({
      message: '评论发表成功 🎉',
      type: 'success',
      duration: 2000
    })
    commentContent.value = ''
    commentPage.value = 1
    fetchComments()
  } catch (e) {
    ElMessage.error('评论发表失败')
    console.error(e)
  } finally {
    submitting.value = false
  }
}

function canDeleteComment(comment) {
  if (!isLoggedIn.value) return false
  return comment.userId === userStore.userInfo?.id || userStore.isAdmin()
}

async function deleteComment(id) {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await commentApi.delete(id)
    ElMessage({
      message: '评论已删除',
      type: 'success',
      duration: 2000
    })
    fetchComments()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function scrollToComments() {
  document.getElementById('comments')?.scrollIntoView({ behavior: 'smooth' })
}

function goUserProfile(userId) {
  router.push(`/user/${userId}`)
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function formatRelativeTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}
</script>

<style lang="scss" scoped>
.blog-detail {
  max-width: 800px;
  margin: 0 auto;
}

.back-nav {
  margin-bottom: 20px;
  
  .el-button {
    color: var(--text-secondary);
    font-size: 15px;
    
    &:hover {
      color: var(--primary-color);
    }
  }
}

// 文章样式
.blog-article {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 40px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.article-header {
  margin-bottom: 32px;
  
  .article-title {
    font-size: 32px;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.4;
    margin-bottom: 24px;
    word-wrap: break-word;
    overflow-wrap: break-word;
  }
  
  .article-meta {
    margin-bottom: 20px;
    
    .author-info {
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer;
      
      &:hover .author-name {
        color: var(--primary-color);
      }
    }
    
    .author-avatar {
      border: 2px solid var(--primary-light);
    }
    
    .author-details {
      .author-name {
        font-weight: 600;
        font-size: 16px;
        color: var(--text-primary);
        transition: color var(--transition-fast);
      }
      
      .publish-info {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-top: 4px;
        font-size: 14px;
        color: var(--text-secondary);
        
        .el-icon {
          font-size: 14px;
        }
        
        .separator {
          color: var(--text-placeholder);
        }
      }
    }
  }
  
  .article-tags {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    
    .tag-item {
      background: rgba(92, 107, 192, 0.08);
      border-color: transparent;
      color: var(--primary-color);
      font-weight: 500;
    }
  }
}

.article-content {
  line-height: 1.9;
  font-size: 17px;
  color: var(--text-regular);
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-word;
  
  :deep(p) {
    margin-bottom: 18px;
    word-wrap: break-word;
    overflow-wrap: break-word;
  }
  
  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: var(--radius-md);
    margin: 20px 0;
  }
  
  :deep(pre) {
    background: #1E1E3F;
    color: #A9B7C6;
    padding: 20px;
    border-radius: var(--radius-md);
    overflow-x: auto;
    margin: 20px 0;
    font-family: 'Fira Code', monospace;
    font-size: 14px;
  }
  
  :deep(code) {
    background: rgba(92, 107, 192, 0.1);
    color: var(--primary-dark);
    padding: 3px 8px;
    border-radius: 4px;
    font-family: 'Fira Code', monospace;
  }
  
  :deep(pre code) {
    background: transparent;
    color: inherit;
    padding: 0;
  }
  
  :deep(blockquote) {
    border-left: 4px solid var(--primary-color);
    padding: 16px 20px;
    margin: 20px 0;
    background: rgba(92, 107, 192, 0.05);
    border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
    color: var(--text-secondary);
    font-style: italic;
  }
}

.article-footer {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);
  
  .action-bar {
    display: flex;
    gap: 16px;
    justify-content: center;
    
    .el-button {
      padding: 12px 28px;
      
      .count {
        margin-left: 8px;
        padding-left: 8px;
        border-left: 1px solid currentColor;
        opacity: 0.7;
      }
    }
    
    .like-btn {
      &.liked {
        background: linear-gradient(135deg, #FF6B6B 0%, #EE5A5A 100%);
        border-color: transparent;
        color: white;
      }
    }
  }
}

// 评论区
.comments-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  
  .section-header {
    margin-bottom: 24px;
    
    h3 {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 20px;
      font-weight: 600;
      color: var(--text-primary);
      
      .el-icon {
        color: var(--primary-color);
      }
    }
  }
}

.comment-form {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);
  
  .form-content {
    flex: 1;
    
    :deep(.el-textarea__inner) {
      border-radius: var(--radius-md);
      padding: 14px;
      font-size: 15px;
    }
    
    .el-button {
      margin-top: 12px;
      float: right;
    }
  }
}

.login-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: 24px;
  color: var(--text-secondary);
  
  a {
    color: var(--primary-color);
    font-weight: 500;
  }
}

.comment-list {
  min-height: 100px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid var(--border-light);
  animation: fadeInUp 0.3s ease forwards;
  opacity: 0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .comment-avatar {
    flex-shrink: 0;
  }
  
  .comment-body {
    flex: 1;
    
    .comment-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      
      .username {
        font-weight: 600;
        font-size: 15px;
        color: var(--text-primary);
      }
      
      .time {
        font-size: 13px;
        color: var(--text-secondary);
      }
    }
    
    .comment-text {
      margin: 0;
      line-height: 1.7;
      color: var(--text-regular);
      font-size: 15px;
    }
    
    .comment-actions {
      margin-top: 8px;
    }
  }
}

.empty-comments {
  text-align: center;
  padding: 48px 20px;
  color: var(--text-secondary);
  
  .el-icon {
    font-size: 48px;
    color: var(--text-placeholder);
    margin-bottom: 12px;
  }
  
  p {
    font-size: 15px;
  }
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式
@media (max-width: 768px) {
  .blog-article {
    padding: 24px;
  }
  
  .article-title {
    font-size: 24px !important;
  }
  
  .article-footer .action-bar {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }
  
  .comments-section {
    padding: 20px;
  }
  
  .comment-form {
    flex-direction: column;
    
    .el-avatar {
      display: none;
    }
  }
}
</style>
