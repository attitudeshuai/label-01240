import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 0) {
      // 401 未登录 - 静默处理，不显示错误消息
      if (res.code === 401) {
        localStorage.removeItem('token')
        // 只有在非首页时才跳转登录页
        if (router.currentRoute.value.meta.auth) {
          router.push('/login')
        }
        return Promise.reject(new Error(res.message))
      }
      
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    console.error('请求错误:', error)
    // 处理 HTTP 错误状态码
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      // 只有在需要认证的页面才跳转
      if (router.currentRoute.value.meta.auth) {
        router.push('/login')
      }
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.message || '网络错误')
    return Promise.reject(error)
  }
)

// 认证相关
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  logout: () => api.post('/auth/logout'),
  getInfo: () => api.get('/auth/info')
}

// 博客相关
export const blogApi = {
  getList: (params) => api.get('/blogs', { params }),
  getDetail: (id) => api.get(`/blogs/${id}`),
  create: (data) => api.post('/blogs', data),
  update: (id, data) => api.put(`/blogs/${id}`, data),
  delete: (id) => api.delete(`/blogs/${id}`),
  batchDelete: (ids) => api.delete('/blogs/batch', { data: ids }),
  getMyBlogs: (params) => api.get('/blogs/my', { params }),
  getUserBlogs: (userId, params) => api.get(`/blogs/user/${userId}`, { params })
}

// 评论相关
export const commentApi = {
  getByBlogId: (blogId, params) => api.get(`/comments/blog/${blogId}`, { params }),
  create: (data) => api.post('/comments', data),
  delete: (id) => api.delete(`/comments/${id}`)
}

// 点赞相关
export const likeApi = {
  toggle: (blogId) => api.post(`/likes/blog/${blogId}`),
  getStatus: (blogId) => api.get(`/likes/blog/${blogId}/status`)
}

// 用户相关
export const userApi = {
  getInfo: (id) => api.get(`/users/${id}`),
  updateProfile: (data) => api.put('/users/profile', data),
  getStats: (id) => api.get(`/users/${id}/stats`)
}

// 上传相关
export const uploadApi = {
  avatar: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/upload/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  image: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// 管理员相关
export const adminApi = {
  getUsers: (params) => api.get('/admin/users', { params }),
  updateUserStatus: (id, status, reason) => api.put(`/admin/users/${id}/status`, null, { params: { status, reason } }),
  getBlogs: (params) => api.get('/admin/blogs', { params }),
  updateBlogStatus: (id, status, reason) => api.put(`/admin/blogs/${id}/status`, null, { params: { status, reason } }),
  getComments: (params) => api.get('/admin/comments', { params }),
  deleteComment: (id, reason) => api.delete(`/admin/comments/${id}`, { params: { reason } }),
  getReviewLogs: (params) => api.get('/admin/review-logs', { params }),
  getReviewHistory: (targetType, targetId) => api.get(`/admin/review-logs/${targetType}/${targetId}`)
}

export default api
