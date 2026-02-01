<template>
  <div class="admin-comments">
    <div class="page-header">
      <h2>评论管理</h2>
    </div>

    <div class="card">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索评论内容" clearable style="width: 200px" @keyup.enter="handleSearch" />
        <el-select v-model="status" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
          <el-option label="正常" :value="1" />
          <el-option label="已删除" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 评论列表 -->
      <el-table :data="comments" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="评论内容" min-width="250">
          <template #default="{ row }">
            <div class="comment-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="评论者" width="120" />
        <el-table-column prop="blogId" label="博客ID" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评论时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="danger"
              text
              size="small"
              @click="deleteComment(row)"
            >
              删除
            </el-button>
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
          @change="fetchComments"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'

const loading = ref(false)
const comments = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref(null)

onMounted(() => {
  fetchComments()
})

async function fetchComments() {
  loading.value = true
  try {
    const res = await adminApi.getComments({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      status: status.value
    })
    comments.value = res.data.list
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchComments()
}

async function deleteComment(comment) {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入删除原因（可选）：', 
      '删除评论',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '如：违规内容、广告信息、恶意攻击等',
        type: 'warning'
      }
    )
    await adminApi.deleteComment(comment.id, reason || '')
    ElMessage.success('删除成功')
    fetchComments()
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
.comment-content {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
