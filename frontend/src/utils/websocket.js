import { Client, Stomp } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { notify } from '../utils/notify'

let client = null
let connected = false
const listeners = {}

export const wsService = {
  connect() {
    if (client && connected) return

    const token = localStorage.getItem('token')
    if (!token) return

    const wsUrl = window.location.protocol === 'https:' 
      ? 'https://' + window.location.host + '/ws' 
      : 'http://' + window.location.host + '/ws'
    
    client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: {
        'user_token': token
      },
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (str) => {
        if (str.includes('DISCONNECTED') || str.includes('CONNECTED') || str.includes('ERROR')) {
          console.log('[WS]', str)
        }
      },
      onConnect: () => {
        connected = true
        console.log('WebSocket 连接成功')
        this.subscribeToUser()
      },
      onDisconnect: () => {
        connected = false
        console.log('WebSocket 断开连接')
        setTimeout(() => this.connect(), 5000)
      },
      onStompError: (frame) => {
        console.error('STOMP 错误:', frame.command, JSON.stringify(frame.headers), frame.body)
      }
    })

    client.activate()
  },

  subscribeToUser() {
    if (!client || !connected) return

    client.subscribe('/user/queue/notification', (message) => {
      try {
        const body = JSON.parse(message.body)
        console.log('[WS] 收到通知:', body)
        this.handleNotification(body)
      } catch (e) {
        console.error('解析通知失败:', e)
      }
    })
  },

  handleNotification(notification) {
    const type = notification.type
    const data = notification.data

    switch (type) {
      case 'NEW_COMMENT':
        notify.success(`${data.username} 评论了你的帖子`)
        this.emit('comment', data)
        break
      case 'NEW_LIKE':
        notify.success(`${data.username} 点赞了你的帖子`)
        this.emit('like', data)
        break
      case 'FRIEND_REQUEST':
        notify.success(`收到来自 ${data.fromUsername} 的好友申请`)
        this.emit('friend_request', data)
        break
      case 'GROUP_INVITE':
        notify.success(`${data.inviterName} 邀请你加入群聊 ${data.groupName}`)
        this.emit('group_invite', data)
        break
      case 'PRIVATE_MESSAGE':
        this.emit('private_message', data)
        break
      case 'POST_UPDATE':
        this.emit('post_update', data)
        break
      case 'NEW_POST':
        this.emit('new_post', data)
        break
      default:
        console.log('[WS] 未知通知类型:', type)
    }
  },

  subscribeGroup(groupId) {
    if (!client || !connected) return

    const topic = `/topic/group-${groupId}`
    console.log('[WS] 订阅群聊:', topic)
    return client.subscribe(topic, (message) => {
      try {
        const body = JSON.parse(message.body)
        this.emit('group_message_' + groupId, body)
      } catch (e) {
        console.error('解析群消息失败:', e)
      }
    })
  },

  unsubscribe(topic) {
    if (client) {
      client.unsubscribe(topic)
    }
  },

  on(event, callback) {
    if (!listeners[event]) {
      listeners[event] = []
    }
    listeners[event].push(callback)
  },

  off(event, callback) {
    if (listeners[event]) {
      listeners[event] = listeners[event].filter(cb => cb !== callback)
    }
  },

  emit(event, data) {
    if (listeners[event]) {
      listeners[event].forEach(cb => cb(data))
    }
  },

  disconnect() {
    if (client) {
      client.deactivate()
      client = null
      connected = false
    }
  }
}