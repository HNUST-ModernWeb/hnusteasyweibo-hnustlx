package com.hnust.lx.service;

import com.hnust.lx.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(Long userId, String type, Object data) {
        NotificationVO notification = new NotificationVO();
        notification.setType(type);
        notification.setData(data);
        notification.setTimestamp(System.currentTimeMillis());
        
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notification", notification);
        log.info("推送消息给用户 {}: type={}", userId, type);
    }

    public void sendToTopic(String topic, String type, Object data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/" + topic, message);
        log.info("广播消息到主题 {}: type={}", topic, type);
    }

    public void notifyNewPost(Long userId, Object postData) {
        sendToUser(userId, "NEW_POST", postData);
    }

    public void notifyNewComment(Long userId, Object commentData) {
        sendToUser(userId, "NEW_COMMENT", commentData);
    }

    public void notifyNewLike(Long userId, Object likeData) {
        sendToUser(userId, "NEW_LIKE", likeData);
    }

    public void notifyFriendRequest(Long userId, Object requestData) {
        sendToUser(userId, "FRIEND_REQUEST", requestData);
    }

    public void notifyGroupInvite(Long userId, Object inviteData) {
        sendToUser(userId, "GROUP_INVITE", inviteData);
    }

    public void notifyPrivateMessage(Long userId, Object messageData) {
        sendToUser(userId, "PRIVATE_MESSAGE", messageData);
    }

    public void broadcastPostUpdate(Object postData) {
        sendToTopic("posts", "POST_UPDATE", postData);
    }

    public void broadcastGroupMessage(Long groupId, Object messageData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "GROUP_MESSAGE");
        message.put("data", messageData);
        message.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/group-" + groupId, message);
    }
}