<template>
  <div class="admin-blogs">
    <div class="page-header">
      <h2>内容管理</h2>
    </div>

    <div class="card">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索标题/标签" clearable style="width: 200px" @keyup.enter="handleSearch" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
          <el-option label="已发布" :value="1" />
          <el-option label="草稿" :value="0" />
          <el-option label="已下架" :value="2" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 博客列表 -->
      <el-table :data="blogs" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/blog/${row.id}`" class="blog-link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                v-if="row.status === 1"
                type="warning"
                text
                size="small"
                @click="updateStatus(row, 2)"
              >
                下架
              </el-button>
              <el-button
                v-if="row.status === 2"
                type="success"
                text
                size="small"
                @click="updateStatus(row, 1)"
              >
                上架
              </el-button>
              <el-button type="danger" text size="small" @click="deleteBlog(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="fetchBlogs"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, blogApi } from '../../api'

const loading = ref(false)
const blogs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref(null)

onMounted(() => {
  fetchBlogs()
})

async function fetchBlogs() {
  loading.value = true
  try {
    const res = await adminApi.getBlogs({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      status: status.value
    })
    blogs.value = res.data.list
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchBlogs()
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'success', 2: 'warning' }
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = { 0: '草稿', 1: '已发布', 2: '已下架' }
  return texts[status] || '未知'
}

async function updateStatus(blog, newStatus) {
  const action = newStatus === 2 ? '下架' : '上架'
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `请输入${action}原因（可选）：`, 
      `${action}博客"${blog.title}"`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '如：内容违规、广告信息等',
        type: 'warning'
      }
    )
    await adminApi.updateBlogStatus(blog.id, newStatus, reason || '')
    ElMessage.success(`${action}成功`)
    fetchBlogs()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function deleteBlog(blog) {
  try {
    await ElMessageBox.confirm(`确定删除博客"${blog.title}"吗？此操作不可恢复！`, '警告', {
      type: 'error'
    })
    await blogApi.delete(blog.id)
    ElMessage.success('删除成功')
    fetchBlogs()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.blog-link {
  color: var(--primary-color);
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}
</style>
