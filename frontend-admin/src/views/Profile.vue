<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="avatar-wrapper">
          <el-avatar :size="100" :src="userInfo?.avatar || ''" class="profile-avatar">
            {{ userInfo?.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <el-tag v-if="userInfo?.role === 1" type="danger" effect="dark" class="role-tag">
            管理员
          </el-tag>
        </div>
        <div class="user-info">
          <h1 class="username">{{ userInfo?.username }}</h1>
          <p class="bio">{{ userInfo?.bio || '这个人很懒，还没有写简介...' }}</p>
          <div class="user-meta">
            <span v-if="userInfo?.email">
              <el-icon><Message /></el-icon>
              {{ userInfo.email }}
            </span>
            <span>
              <el-icon><Calendar /></el-icon>
              {{ formatTime(userInfo?.createdAt) }} 加入
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="8" :md="6">
          <div class="stats-card">
            <h3>数据统计</h3>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-icon articles">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value">{{ userInfo?.blogCount || 0 }}</span>
                  <span class="stat-label">文章</span>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon likes">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value">{{ userInfo?.likeCount || 0 }}</span>
                  <span class="stat-label">获赞</span>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="16" :md="18">
          <div class="edit-card">
            <div class="card-header">
              <h3><el-icon><Setting /></el-icon>编辑资料</h3>
            </div>
            
            <el-form :model="form" label-position="top" class="profile-form">
              <el-form-item label="头像">
                <div class="avatar-upload">
                  <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="uploadAvatar"
                  >
                    <el-avatar :size="80" :src="form.avatar || ''" v-if="form.avatar" />
                    <div v-else class="upload-placeholder">
                      <el-icon><Plus /></el-icon>
                      <span>上传头像</span>
                    </div>
                  </el-upload>
                  <span class="upload-tip">支持 JPG、PNG、GIF 格式，最大 5MB</span>
                </div>
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input v-model="form.bio" type="textarea" maxlength="500" show-word-limit />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loading" @click="handleSave">保存</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { userApi, uploadApi } from '../api'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const loading = ref(false)
const form = reactive({ avatar: '', bio: '' })

watch(userInfo, (val) => {
  if (val) {
    form.avatar = val.avatar || ''
    form.bio = val.bio || ''
  }
}, { immediate: true })

function beforeAvatarUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('图片格式错误'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过 5MB'); return false }
  return true
}

async function uploadAvatar(options) {
  try {
    const res = await uploadApi.avatar(options.file)
    form.avatar = res.data.url
    ElMessage.success('头像上传成功 📷')
  } catch (e) { ElMessage.error('头像上传失败') }
}

async function handleSave() {
  loading.value = true
  try {
    await userApi.updateProfile(form)
    ElMessage.success('资料保存成功 ✨')
    await userStore.fetchUserInfo()
  } catch (e) { ElMessage.error('保存失败') }
  finally { loading.value = false }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
}
</script>

<style lang="scss" scoped>
.profile-page { max-width: 1200px; margin: -32px auto 0; }

.profile-header {
  position: relative; margin-bottom: 32px;
  .header-bg { height: 80px; background: var(--primary-gradient); border-radius: 0 0 var(--radius-xl) var(--radius-xl); }
  .header-content { display: flex; align-items: flex-end; gap: 24px; padding: 0 32px; margin-top: -60px; position: relative; z-index: 1; }
  .avatar-wrapper { position: relative;
    .profile-avatar { border: 4px solid var(--bg-card); box-shadow: var(--shadow-md); }
    .role-tag { position: absolute; bottom: -4px; left: 50%; transform: translateX(-50%); }
  }
  .user-info { flex: 1; padding-bottom: 16px; padding-top: 20px;
    .username { font-size: 24px; font-weight: 700; color: #333; margin-bottom: 6px; }
    .bio { color: #333; font-size: 14px; margin-bottom: 10px; }
    .user-meta { display: flex; gap: 24px; font-size: 13px; color: #333;
      span { display: flex; align-items: center; gap: 6px; }
      .el-icon { color: #666; }
    }
  }
}

.profile-content { padding: 0 16px; }

.stats-card {
  background: var(--bg-card); border-radius: var(--radius-lg); padding: 24px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); height: 320px; box-sizing: border-box;
  h3 { font-size: 16px; font-weight: 600; margin-bottom: 20px; }
  .stats-grid { display: flex; flex-direction: column; gap: 16px; }
  .stat-item { display: flex; align-items: center; gap: 16px; padding: 16px; background: var(--bg-secondary); border-radius: var(--radius-md);
    .stat-icon { display: flex; align-items: center; justify-content: center; font-size: 22px; width: 48px; height: 48px; border-radius: 12px;
      &.articles { background: var(--primary-gradient); color: white; }
      &.likes { background: linear-gradient(135deg, #FF6B6B 0%, #EE5A5A 100%); color: white; }
    }
    .stat-info { .stat-value { display: block; font-size: 24px; font-weight: 700; } .stat-label { font-size: 13px; color: var(--text-secondary); } }
  }
}

.edit-card {
  background: var(--bg-card); border-radius: var(--radius-lg); padding: 32px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); min-height: 320px; box-sizing: border-box;
  .card-header { margin-bottom: 28px;
    h3 { display: flex; align-items: center; gap: 8px; font-size: 18px; color: var(--text-primary); .el-icon { color: var(--primary-color); } }
  }
}

.profile-form { max-width: 600px; }

.avatar-upload {
  display: flex; flex-direction: column; align-items: flex-start; gap: 8px;
  .avatar-uploader { cursor: pointer;
    :deep(.el-upload) { border: 1px dashed var(--border-color); border-radius: 8px; overflow: hidden; transition: border-color 0.3s; &:hover { border-color: var(--primary-color); } }
  }
  .upload-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; width: 80px; height: 80px; background: var(--bg-secondary); color: var(--text-secondary); .el-icon { font-size: 24px; margin-bottom: 4px; } }
  .upload-tip { font-size: 12px; color: var(--text-secondary); }
}

@media (max-width: 768px) {
  .profile-header .header-content { flex-direction: column; align-items: center; text-align: center; padding: 0 16px; }
  .edit-card { padding: 20px; }
}
</style>
