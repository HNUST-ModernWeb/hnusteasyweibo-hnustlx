import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const userToken = localStorage.getItem('token')
  const adminToken = localStorage.getItem('admin_token')
  
  // 管理员接口用 admin_token，普通接口用 user_token
  if (config.url.includes('/admin/') || config.url.includes('/tag/')) {
    config.headers['admin_token'] = adminToken || userToken
  } else {
    config.headers['user_token'] = userToken
  }
  return config
})

api.interceptors.response.use(
  response => response.data,
  error => {
    const msg = error.response?.data?.msg || '请求失败'
    if (error.response?.status !== 401) {
      alert(msg)
    }
    return Promise.reject(error)
  }
)

export default api

export const userApi = {
  register: (data) => api.post('/user/register', data),
  login: (data) => api.post('/user/login', data),
  getInfo: (userId) => api.get('/user/info', { params: { userId } }),
  getInfoById: (userId) => api.get('/user/info/query', { params: { userId } }),
  getCurrentUserInfo: () => {
    const userId = localStorage.getItem('userId')
    return api.get('/user/info', { params: { userId } })
  },
  update: (data) => api.put('/user/update', data),
  uploadAvatar: (formData) => api.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  getHot: (limit = 5) => api.get('/user/hot', { params: { limit } })
}

export const postApi = {
  add: (data) => api.post('/post/add', data),
  list: (params) => api.get('/post/list', { params }),
  detail: (postId) => api.get(`/post/detail/${postId}`),
  delete: (data) => api.delete('/post/delete', { data }),
  update: (data) => api.put('/post/update', data),
  uploadImage: (formData) => api.post('/post/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  getUserTotalLikes: (userId) => api.get(`/post/user/${userId}`)
}

export const commentApi = {
  add: (data) => api.post('/comment/add', data),
  list: (postId, params) => api.get(`/comment/list/${postId}`, { params }),
  delete: (data) => api.delete('/comment/delete', { data })
}

export const likeApi = {
  toggle: (data) => api.post('/like/toggle', data),
  myList: (params) => api.get('/like/my-list', { params }),
  postList: (postId, params) => api.get(`/like/post-list/${postId}`, { params })
}

export const tagApi = {
  list: (params) => api.get('/tag/list', { params }),
  add: (data) => api.post('/tag/add', data),
  getHot: (limit = 9) => api.get('/tag/hot', { params: { limit } })
}

export const postTagApi = {
  add: (data) => api.post('/post-tag/add', data),
  getList: (postId) => api.get(`/post-tag/list/${postId}`),
  delete: (data) => api.post('/post-tag/delete', data)
}

export const adminApi = {
  login: (data) => api.post('/admin/login', data),
  getStats: () => api.get('/admin/stats'),
  listUsers: (params) => api.get('/admin/user/list', { params }),
  updateUserStatus: (data) => api.put('/admin/user/status', data),
  deleteUser: (id) => api.delete(`/admin/user/${id}`),
  addTag: (data) => api.post('/tag/add', data),
  deleteTag: (data) => api.post('/tag/delete', data),
  listTags: (params) => api.get('/tag/list', { params })
}

export const searchApi = {
  users: (keyword, limit = 20) => api.get('/search/user', { params: { keyword, limit } }),
  posts: (keyword, limit = 20) => api.get('/search/post', { params: { keyword, limit } })
}