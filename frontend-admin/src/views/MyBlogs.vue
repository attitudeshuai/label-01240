<template>
  <div class="my-blogs">
    <div class="page-header">
      <h2>我的博客</h2>
      <el-button type="primary" icon="Plus" @click="$router.push('/write')">写博客</el-button>
    </div>

    <div class="card">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索标题" clearable style="width: 200px" @keyup.enter="handleSearch" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
          <el-option label="已发布" :value="1" />
          <el-option label="草稿" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button 
          type="danger" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          批量删除 ({{ selectedIds.length }})
        </el-button>
      </div>

      <!-- 博客列表 -->
      <el-table 
        :data="blogs" 
        v-loading="loading" 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/blog/${row.id}`" class="blog-link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" link size="small" @click="editBlog(row.id)">编辑</el-button>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { blogApi } from '../api'

const router = useRouter()

const loading = ref(false)
const blogs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref(null)
const selectedIds = ref([])

onMounted(() => {
  fetchBlogs()
})

async function fetchBlogs() {
  loading.value = true
  try {
    const res = await blogApi.getMyBlogs({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      status: status.value
    })
    blogs.value = res.data.list
    total.value = res.data.total
    selectedIds.value = []
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

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function editBlog(id) {
  router.push(`/edit/${id}`)
}

async function deleteBlog(blog) {
  try {
    await ElMessageBox.confirm(`确定删除博客"${blog.title}"吗？`, '提示', {
      type: 'warning'
    })
    await blogApi.delete(blog.id)
    ElMessage.success('删除成功')
    fetchBlogs()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedIds.value.length} 篇博客吗？此操作不可恢复。`, 
      '批量删除确认', 
      { type: 'warning' }
    )
    
    loading.value = true
    await blogApi.batchDelete(selectedIds.value)
    ElMessage.success(`成功删除 ${selectedIds.value.length} 篇博客`)
    fetchBlogs()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  } finally {
    loading.value = false
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
