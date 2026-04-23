import axios from 'axios'
import { notify } from '../utils/notify'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加token
api.interceptors.request.use(config => {
  const userToken = localStorage.getItem('token')
  if (userToken) {
    config.headers['user_token'] = userToken
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // token失效，跳转登录
      localStorage.clear()
      window.location.href = '/login'
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
  delete: (data) => api.delete('/comment/delete', { data }),
  received: (params) => api.get('/comment/received', { params })
}

export const likeApi = {
  toggle: (data) => api.post('/like/toggle', data),
  myList: (params) => api.get('/like/my-list', { params }),
  postList: (postId, params) => api.get(`/like/post-list/${postId}`, { params }),
  received: (params) => api.get('/like/received', { params })
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

export const chatApi = {
  sendPrivate: (data) => api.post('/message/private/send', data),
  privateList: (userId, params) => api.get(`/message/private/list/${userId}`, { params }),
  conversations: (params) => api.get('/message/private/conversations', { params }),
  unreadCount: () => api.get('/message/private/unread-count'),
  markRead: (data) => api.put('/message/private/mark-read', data),
  createGroup: (data) => api.post('/group/create', data),
  groupList: (params) => api.get('/group/list', { params }),
  joinGroup: (groupId) => api.post(`/group/${groupId}/join`),
  leaveGroup: (groupId) => api.post(`/group/${groupId}/leave`),
  groupMembers: (groupId, params) => api.get(`/group/${groupId}/members`, { params }),
  sendGroupMessage: (groupId, data) => api.post(`/group/${groupId}/message/send`, data),
  groupMessageList: (groupId, params) => api.get(`/group/${groupId}/message/list`, { params }),
  inviteToGroup: (groupId, data) => api.post(`/group/${groupId}/invite`, data),
  pendingInvites: (params) => api.get('/group/invites', { params }),
  acceptInvite: (inviteId) => api.post('/group/invite/accept', { inviteId }),
  rejectInvite: (inviteId) => api.post('/group/invite/reject', { inviteId }),
  inviteCount: () => api.get('/group/invite/count')
}

export const friendApi = {
  sendRequest: (userId) => api.post('/friend/request', { userId }),
  accept: (requestId) => api.post('/friend/accept', { requestId }),
  reject: (requestId) => api.post('/friend/reject', { requestId }),
  delete: (userId) => api.delete('/friend/delete', { data: { userId } }),
  list: (params) => api.get('/friend/list', { params }),
  requests: (params) => api.get('/friend/requests', { params }),
  count: () => api.get('/friend/count'),
  check: (userId) => api.get('/friend/check', { params: { userId } }),
  isFriend: (userId) => api.get('/friend/check', { params: { userId } })
}