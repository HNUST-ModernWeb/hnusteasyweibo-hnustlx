import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Publish from '../views/Publish.vue'
import PostDetail from '../views/PostDetail.vue'
import Profile from '../views/Profile.vue'
import Search from '../views/Search.vue'
import Discover from '../views/Discover.vue'
import Messages from '../views/Messages.vue'
import AdminLogin from '../views/AdminLogin.vue'
import AdminLayout from '../components/AdminLayout.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import AdminUsers from '../views/AdminUsers.vue'
import AdminTags from '../views/AdminTags.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/login', name: 'Login', component: Login },
  { path: '/publish', name: 'Publish', component: Publish, meta: { requiresAuth: true } },
  { path: '/post/:postId', name: 'PostDetail', component: PostDetail },
  { path: '/profile/:userId', name: 'Profile', component: Profile },
  { path: '/search', name: 'Search', component: Search },
  { path: '/discover', name: 'Discover', component: Discover },
  { path: '/messages', name: 'Messages', component: Messages, meta: { requiresAuth: true } },
  
  { path: '/admin/login', name: 'AdminLogin', component: AdminLogin },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAdmin: true },
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: AdminDashboard },
      { path: 'users', name: 'AdminUsers', component: AdminUsers },
      { path: 'tags', name: 'AdminTags', component: AdminTags },
      { path: '', redirect: '/admin/dashboard' }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  const adminToken = localStorage.getItem('admin_token')
  
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
  
  if (to.meta.requiresAdmin && !adminToken) {
    return '/admin/login'
  }
})

export default router
