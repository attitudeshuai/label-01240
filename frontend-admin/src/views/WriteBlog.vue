<template>
  <div class="write-blog">
    <div class="card">
      <h2>{{ isEdit ? '编辑博客' : '写博客' }}</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入博客标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如：Vue,前端,教程" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <div class="editor-container" @click="handleContainerClick">
            <Toolbar
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
            />
            <Editor
              v-model="form.content"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '发布博客' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { blogApi } from '../api'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const blogId = computed(() => route.params.id)

const formRef = ref()
const loading = ref(false)
const editorRef = shallowRef()

const form = reactive({
  title: '',
  content: '',
  tags: '',
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const toolbarConfig = {
  excludeKeys: ['uploadVideo']
}

const editorConfig = {
  placeholder: '请输入博客内容...',
  MENU_CONF: {
    uploadImage: {
      // 使用服务器上传API
      server: '/api/upload/image',
      fieldName: 'file',
      maxFileSize: 5 * 1024 * 1024, // 5MB
      allowedFileTypes: ['image/*'],
      // 自定义上传
      async customUpload(file, insertFn) {
        const formData = new FormData()
        formData.append('file', file)
        try {
          const response = await fetch('/api/upload/image', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: formData
          })
          const result = await response.json()
          if (result.code === 200 && result.data?.url) {
            insertFn(result.data.url, file.name, '')
          } else {
            ElMessage.error(result.message || '图片上传失败')
          }
        } catch (error) {
          console.error('图片上传失败:', error)
          ElMessage.error('图片上传失败')
        }
      }
    }
  }
}

function handleCreated(editor) {
  editorRef.value = editor
}

function handleContainerClick(e) {
  // 如果点击的是工具栏，不进行处理
  if (e.target.closest('.w-e-toolbar')) return
  // 如果编辑器实例存在，聚焦编辑器
  editorRef.value?.focus()
}

onMounted(() => {
  if (isEdit.value) {
    fetchBlog()
  }
})

onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
  }
})

async function fetchBlog() {
  try {
    const res = await blogApi.getDetail(blogId.value)
    form.title = res.data.title
    form.content = res.data.content
    form.tags = res.data.tags || ''
    form.status = res.data.status
  } catch (e) {
    console.error(e)
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.content || form.content === '<p><br></p>') {
    ElMessage.warning('请输入博客内容')
    return
  }

  loading.value = true
  try {
    if (isEdit.value) {
      await blogApi.update(blogId.value, form)
      ElMessage.success('修改成功')
    } else {
      await blogApi.create(form)
      ElMessage.success('发布成功')
    }
    router.push('/my-blogs')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.write-blog {
  max-width: 900px;
  margin: 0 auto;

  h2 {
    margin: 0 0 24px;
    font-size: 20px;
  }
}

.editor-container {
  border: 1px solid var(--border-color);
  border-radius: 4px;
  overflow: hidden;
  width: 100%;

  :deep(.w-e-toolbar) {
    border-bottom: 1px solid var(--border-color) !important;
  }

  :deep(.w-e-text-container) {
    min-height: 400px !important;
  }
}
</style>
