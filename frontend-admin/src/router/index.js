import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'blog/:id',
        name: 'BlogDetail',
        component: () => import('../views/BlogDetail.vue'),
        meta: { title: '博客详情' }
      },
      {
        path: 'write',
        name: 'WriteBlog',
        component: () => import('../views/WriteBlog.vue'),
        meta: { title: '写博客', auth: true }
      },
      {
        path: 'edit/:id',
        name: 'EditBlog',
        component: () => import('../views/WriteBlog.vue'),
        meta: { title: '编辑博客', auth: true }
      },
      {
        path: 'my-blogs',
        name: 'MyBlogs',
        component: () => import('../views/MyBlogs.vue'),
        meta: { title: '我的博客', auth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', auth: true }
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('../views/UserProfile.vue'),
        meta: { title: '用户主页' }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue'),
        meta: { title: '用户管理', auth: true, admin: true }
      },
      {
        path: 'admin/blogs',
        name: 'AdminBlogs',
        component: () => import('../views/admin/Blogs.vue'),
        meta: { title: '内容管理', auth: true, admin: true }
      },
      {
        path: 'admin/comments',
        name: 'AdminComments',
        component: () => import('../views/admin/Comments.vue'),
        meta: { title: '评论管理', auth: true, admin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  // 已登录访问登录/注册页
  if (to.meta.guest && token) {
    return next('/home')
  }

  // 需要登录的页面
  if (to.meta.auth && !token) {
    return next('/login')
  }

  // 获取用户信息
  if (token && !userStore.userInfo) {
    await userStore.fetchUserInfo()
  }

  // 需要管理员权限
  if (to.meta.admin && !userStore.isAdmin()) {
    return next('/home')
  }

  next()
})

export default router
