package com.hnust.lx.service;

import com.hnust.lx.dto.*;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.vo.*;

public interface ChatService {

    PrivateMessageVO sendPrivateMessage(Long senderId, PrivateMessageSendDTO dto);

    PageResult getPrivateMessageList(Long currentUserId, Long otherUserId, Long page, Long pageSize);

    PageResult getConversationList(Long userId, Long page, Long pageSize);

    Integer getUnreadCount(Long userId);

    Integer markAsRead(Long currentUserId, Long fromUserId);

    GroupChatVO createGroup(Long creatorId, GroupCreateDTO dto);

    PageResult getGroupList(Long userId, Long page, Long pageSize);

    void joinGroup(Long userId, Long groupId);

    void leaveGroup(Long userId, Long groupId);

    PageResult getGroupMembers(Long groupId, Long page, Long pageSize);

    GroupMessageVO sendGroupMessage(Long userId, Long groupId, String content);

    PageResult getGroupMessageList(Long groupId, Long page, Long pageSize);

    void inviteToGroup(Long userId, Long groupId, Long inviteUserId);

    PageResult getPendingInvites(Long userId, Long page, Long pageSize);

    void acceptInvite(Long userId, Long inviteId);

    void rejectInvite(Long userId, Long inviteId);

    Integer getInviteCount(Long userId);
}