<template>
  <div class="user-profile" v-loading="loading">
    <template v-if="user">
      <!-- 用户信息卡片 -->
      <div class="card user-card">
        <div class="user-header">
          <el-avatar :size="80" :src="user.avatar || ''">
            {{ user.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div class="user-info">
            <h2>{{ user.username }}</h2>
            <p class="bio">{{ user.bio || '这个人很懒，什么都没写~' }}</p>
            <div class="stats">
              <span><strong>{{ user.blogCount || 0 }}</strong> 文章</span>
              <span><strong>{{ user.likeCount || 0 }}</strong> 获赞</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 用户博客列表 -->
      <div class="card">
        <h3>TA的博客</h3>
        <div class="blog-list" v-loading="blogsLoading">
          <template v-if="blogs.length > 0">
            <div v-for="blog in blogs" :key="blog.id" class="blog-item" @click="goDetail(blog.id)">
              <h4>{{ blog.title }}</h4>
              <div class="blog-meta">
                <span><el-icon><Calendar /></el-icon> {{ formatTime(blog.createdAt) }}</span>
                <span><el-icon><View /></el-icon> {{ blog.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ blog.likeCount }}</span>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无博客" />
        </div>

        <div class="pagination" v-if="total > 10">
          <el-pagination
            v-model:current-page="page"
            :total="total"
            :page-size="10"
            layout="prev, pager, next"
            @change="fetchBlogs"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi, blogApi } from '../api'

const route = useRoute()
const router = useRouter()

const userId = computed(() => route.params.id)

const loading = ref(false)
const user = ref(null)

const blogsLoading = ref(false)
const blogs = ref([])
const total = ref(0)
const page = ref(1)

watch(userId, () => {
  fetchUser()
  fetchBlogs()
}, { immediate: true })

async function fetchUser() {
  loading.value = true
  try {
    const res = await userApi.getInfo(userId.value)
    user.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function fetchBlogs() {
  blogsLoading.value = true
  try {
    const res = await blogApi.getUserBlogs(userId.value, {
      page: page.value,
      size: 10
    })
    blogs.value = res.data.list
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    blogsLoading.value = false
  }
}

function goDetail(id) {
  router.push(`/blog/${id}`)
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.user-profile {
  max-width: 800px;
  margin: 0 auto;
}

.user-card {
  .user-header {
    display: flex;
    gap: 24px;
    align-items: flex-start;
  }

  .user-info {
    flex: 1;

    h2 {
      margin: 0 0 8px;
    }

    .bio {
      color: var(--text-secondary);
      margin: 0 0 16px;
    }

    .stats {
      display: flex;
      gap: 24px;

      span {
        color: var(--text-secondary);

        strong {
          color: var(--text-primary);
          margin-right: 4px;
        }
      }
    }
  }
}

h3 {
  margin: 0 0 20px;
  font-size: 18px;
}

.blog-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background-color 0.3s;

  &:hover {
    background-color: #f5f7fa;
    margin: 0 -20px;
    padding: 16px 20px;
  }

  &:last-child {
    border-bottom: none;
  }

  h4 {
    margin: 0 0 8px;
    font-size: 16px;
    color: var(--text-primary);
  }

  .blog-meta {
    display: flex;
    gap: 16px;
    color: var(--text-secondary);
    font-size: 14px;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}
</style>
