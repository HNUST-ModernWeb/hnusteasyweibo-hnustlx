package com.hnust.lx.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hnust.lx.dto.*;
import com.hnust.lx.entity.*;
import com.hnust.lx.mapper.*;
import com.hnust.lx.result.PageResult;
import com.hnust.lx.service.ChatService;
import com.hnust.lx.service.WebSocketNotificationService;
import com.hnust.lx.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final PrivateMessageMapper privateMessageMapper;
    private final UserMapper userMapper;
    private final GroupChatMapper groupChatMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final GroupInviteMapper groupInviteMapper;
    private final WebSocketNotificationService notificationService;

    @Value("${weibo.web.upload-path}")
    private String uploadPath;

    @Override
    @Transactional
    public PrivateMessageVO sendPrivateMessage(Long senderId, PrivateMessageSendDTO dto) {
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent());
        message.setIsRead(0);
        message.setIsDeleted(0);
        privateMessageMapper.insert(message);

        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setMessageId(message.getMessageId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setSendTime(message.getSendTime());
        vo.setIsRead(0);
        
        User sender = userMapper.findById(senderId);
        if (sender != null) {
            vo.setSenderAvatar(sender.getAvatar());
        }
        
        notificationService.notifyPrivateMessage(dto.getReceiverId(), java.util.Map.of(
            "messageId", message.getMessageId(),
            "senderId", senderId,
            "content", dto.getContent(),
            "sendTime", message.getSendTime() != null ? message.getSendTime().toString() : "",
            "senderName", sender != null ? sender.getUsername() : null,
            "senderAvatar", sender != null ? sender.getAvatar() : null
        ));
        
        return vo;
    }

    @Override
    public PageResult getPrivateMessageList(Long currentUserId, Long otherUserId, Long page, Long pageSize) {
        List<PrivateMessage> allMessages = privateMessageMapper.findConversation(currentUserId, otherUserId);
        User sender = userMapper.findById(currentUserId);
        User receiver = userMapper.findById(otherUserId);
        List<PrivateMessageVO> records = allMessages.stream().map(m -> {
            PrivateMessageVO vo = new PrivateMessageVO();
            vo.setMessageId(m.getMessageId());
            vo.setSenderId(m.getSenderId());
            vo.setReceiverId(m.getReceiverId());
            vo.setContent(m.getContent());
            vo.setSendTime(m.getSendTime());
            vo.setIsRead(m.getIsRead());
            if (m.getSenderId().equals(currentUserId)) {
                vo.setSenderAvatar(sender != null ? sender.getAvatar() : null);
                vo.setReceiverAvatar(receiver != null ? receiver.getAvatar() : null);
            } else {
                vo.setSenderAvatar(receiver != null ? receiver.getAvatar() : null);
                vo.setReceiverAvatar(sender != null ? sender.getAvatar() : null);
            }
            return vo;
        }).collect(Collectors.toList());
        return new PageResult(records.size(), records);
    }

    @Override
    public PageResult getConversationList(Long userId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<Long> userIds = privateMessageMapper.findConversationUsers(userId);
        PageInfo<Long> pageInfo = new PageInfo<>(userIds);

        List<ConversationVO> records = new ArrayList<>();
        for (Long otherUserId : userIds) {
            User user = userMapper.findById(otherUserId);
            if (user != null) {
                List<PrivateMessage> messages = privateMessageMapper.findConversation(userId, otherUserId);
                if (!messages.isEmpty()) {
                    PrivateMessage lastMsg = messages.get(0);
                    int unread = privateMessageMapper.countUnread(otherUserId, userId);

                    ConversationVO vo = new ConversationVO();
                    vo.setUserId(otherUserId);
                    vo.setUsername(user.getUsername());
                    vo.setAvatar(user.getAvatar());
                    vo.setLastMessage(lastMsg.getContent());
                    vo.setLastMessageTime(lastMsg.getSendTime());
                    vo.setUnreadCount(unread);
                    records.add(vo);
                }
            }
        }
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return privateMessageMapper.countTotalUnread(userId);
    }

    @Override
    public Integer markAsRead(Long currentUserId, Long fromUserId) {
        return privateMessageMapper.markAsRead(fromUserId, currentUserId);
    }

    @Override
    @Transactional
    public GroupChatVO createGroup(Long creatorId, GroupCreateDTO dto) {
        GroupChat groupChat = new GroupChat();
        groupChat.setGroupName(dto.getGroupName());
        groupChat.setCreatorId(creatorId);
        groupChat.setAvatar(dto.getAvatar());
        groupChat.setIsDeleted(0);
        groupChatMapper.insert(groupChat);

        GroupMember member = new GroupMember();
        member.setGroupId(groupChat.getGroupId());
        member.setUserId(creatorId);
        member.setRole(2);
        groupMemberMapper.insert(member);

        GroupChatVO vo = new GroupChatVO();
        vo.setGroupId(groupChat.getGroupId());
        vo.setGroupName(groupChat.getGroupName());
        vo.setAvatar(groupChat.getAvatar());
        return vo;
    }

    @Override
    @Transactional
    public GroupChatVO updateGroup(Long userId, Long groupId, GroupCreateDTO dto) {
        GroupChat groupChat = groupChatMapper.findById(groupId);
        if (groupChat == null) {
            throw new RuntimeException("群不存在");
        }
        if (!groupChat.getCreatorId().equals(userId)) {
            throw new RuntimeException("只有群主可以修改群信息");
        }
        
        if (dto.getGroupName() != null && !dto.getGroupName().isEmpty()) {
            groupChat.setGroupName(dto.getGroupName());
        }
        if (dto.getAvatar() != null) {
            groupChat.setAvatar(dto.getAvatar());
        }
        
        groupChatMapper.update(groupChat);
        
        GroupChatVO vo = new GroupChatVO();
        vo.setGroupId(groupChat.getGroupId());
        vo.setGroupName(groupChat.getGroupName());
        vo.setAvatar(groupChat.getAvatar());
        return vo;
    }

    @Override
    public GroupChatVO getGroupInfo(Long groupId) {
        GroupChat groupChat = groupChatMapper.findById(groupId);
        if (groupChat == null) {
            throw new RuntimeException("群不存在");
        }
        GroupChatVO vo = new GroupChatVO();
        vo.setGroupId(groupChat.getGroupId());
        vo.setGroupName(groupChat.getGroupName());
        vo.setAvatar(groupChat.getAvatar());
        return vo;
    }

    @Override
    public String uploadGroupAvatar(Long userId, Long groupId, MultipartFile file) {
        GroupChat groupChat = groupChatMapper.findById(groupId);
        if (groupChat == null) {
            throw new RuntimeException("群不存在");
        }
        if (!groupChat.getCreatorId().equals(userId)) {
            throw new RuntimeException("只有群主可以修改群头像");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        File uploadDir = new File(uploadPath + "group/");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        try {
            File destFile = new File(uploadDir, fileName);
            file.transferTo(destFile);
            
            String avatarUrl = "/upload/group/" + fileName;
            groupChat.setAvatar(avatarUrl);
            groupChatMapper.update(groupChat);
            
            return avatarUrl;
        } catch (Exception e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult getGroupList(Long userId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<GroupChat> groups = groupChatMapper.findByUserId(userId);
        PageInfo<GroupChat> pageInfo = new PageInfo<>(groups);

        List<GroupChatVO> records = new ArrayList<>();
        for (GroupChat g : groups) {
            GroupMessage lastMsg = groupMessageMapper.findLastByGroupId(g.getGroupId());
            int memberCount = groupMemberMapper.countByGroupId(g.getGroupId());

            GroupChatVO vo = new GroupChatVO();
            vo.setGroupId(g.getGroupId());
            vo.setGroupName(g.getGroupName());
            vo.setAvatar(g.getAvatar());
            vo.setMemberCount((long) memberCount);
            if (lastMsg != null) {
                vo.setLastMessage(lastMsg.getContent());
                vo.setLastMessageTime(lastMsg.getSendTime());
            }
            records.add(vo);
        }
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    @Transactional
    public void joinGroup(Long userId, Long groupId) {
        GroupChat groupChat = groupChatMapper.findById(groupId);
        if (groupChat == null) {
            throw new RuntimeException("群不存在");
        }
        if (groupMemberMapper.exists(groupId, userId) > 0) {
            throw new RuntimeException("已在群中");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(0);
        groupMemberMapper.insert(member);
    }

    @Override
    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        GroupChat groupChat = groupChatMapper.findById(groupId);
        if (groupChat == null) {
            throw new RuntimeException("群不存在");
        }
        if (groupMemberMapper.exists(groupId, userId) == 0) {
            throw new RuntimeException("不在群中");
        }
        if (groupChat.getCreatorId().equals(userId)) {
            throw new RuntimeException("群主不能退出");
        }

        groupMemberMapper.delete(groupId, userId);
    }

    @Override
    public PageResult getGroupMembers(Long groupId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<GroupMember> members = groupMemberMapper.findByGroupId(groupId);
        PageInfo<GroupMember> pageInfo = new PageInfo<>(members);

        List<GroupMemberVO> records = new ArrayList<>();
        for (GroupMember m : members) {
            User user = userMapper.findById(m.getUserId());
            if (user != null) {
                GroupMemberVO vo = new GroupMemberVO();
                vo.setUserId(m.getUserId());
                vo.setUsername(user.getUsername());
                vo.setAvatar(user.getAvatar());
                vo.setRole(m.getRole());
                vo.setJoinTime(m.getJoinTime());
                records.add(vo);
            }
        }
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    @Transactional
    public GroupMessageVO sendGroupMessage(Long userId, Long groupId, String content) {
        if (groupMemberMapper.exists(groupId, userId) == 0) {
            throw new RuntimeException("不在群中");
        }

        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setSenderId(userId);
        message.setContent(content);
        groupMessageMapper.insert(message);

        User user = userMapper.findById(userId);

        GroupMessageVO vo = new GroupMessageVO();
        vo.setMessageId(message.getMessageId());
        vo.setGroupId(groupId);
        vo.setSenderId(userId);
        vo.setSenderName(user != null ? user.getUsername() : null);
        vo.setSenderAvatar(user != null ? user.getAvatar() : null);
        vo.setContent(content);
        vo.setSendTime(message.getSendTime());
        
        notificationService.broadcastGroupMessage(groupId, vo);
        
        return vo;
    }

    @Override
    public PageResult getGroupMessageList(Long groupId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<GroupMessage> messages = groupMessageMapper.findByGroupId(groupId);
        PageInfo<GroupMessage> pageInfo = new PageInfo<>(messages);

        List<GroupMessageVO> records = new ArrayList<>();
        for (GroupMessage m : messages) {
            User user = userMapper.findById(m.getSenderId());
            GroupMessageVO vo = new GroupMessageVO();
            vo.setMessageId(m.getMessageId());
            vo.setGroupId(m.getGroupId());
            vo.setSenderId(m.getSenderId());
            vo.setSenderName(user != null ? user.getUsername() : null);
            vo.setSenderAvatar(user != null ? user.getAvatar() : null);
            vo.setContent(m.getContent());
            vo.setSendTime(m.getSendTime());
            records.add(vo);
        }
        return new PageResult(pageInfo.getTotal(), records);
    }

    @Override
    @Transactional
    public void inviteToGroup(Long userId, Long groupId, Long inviteUserId) {
        GroupChat groupChat = groupChatMapper.findById(groupId);
        if (groupChat == null) {
            throw new RuntimeException("群不存在");
        }
        if (groupMemberMapper.exists(groupId, userId) == 0) {
            throw new RuntimeException("你不在群中");
        }
        if (groupMemberMapper.exists(groupId, inviteUserId) > 0) {
            throw new RuntimeException("对方已在群中");
        }
        if (groupInviteMapper.existsPending(groupId, inviteUserId) > 0) {
            throw new RuntimeException("对方有待处理的邀请");
        }

        GroupInvite invite = new GroupInvite();
        invite.setGroupId(groupId);
        invite.setInvitedUserId(inviteUserId);
        invite.setInviterId(userId);
        invite.setStatus(0);
        groupInviteMapper.insert(invite);
        
        GroupChat group = groupChatMapper.findById(groupId);
        User inviter = userMapper.findById(userId);
        notificationService.notifyGroupInvite(inviteUserId, java.util.Map.of(
            "inviteId", invite.getId(),
            "groupId", groupId,
            "groupName", group != null ? group.getGroupName() : null,
            "inviterId", userId,
            "inviterName", inviter != null ? inviter.getUsername() : null,
            "inviterAvatar", inviter != null ? inviter.getAvatar() : null
        ));
    }

    @Override
    public PageResult getPendingInvites(Long userId, Long page, Long pageSize) {
        PageHelper.startPage(page.intValue(), pageSize.intValue());
        List<GroupInvite> invites = groupInviteMapper.findPendingByUserId(userId);
        PageInfo<GroupInvite> pageInfo = new PageInfo<>(invites);

        List<GroupInviteVO> voList = new ArrayList<>();
        for (GroupInvite invite : invites) {
            GroupInviteVO vo = new GroupInviteVO();
            vo.setInviteId(invite.getId());
            vo.setGroupId(invite.getGroupId());
            vo.setInviterId(invite.getInviterId());
            vo.setCreateTime(invite.getCreateTime());

            GroupChat group = groupChatMapper.findById(invite.getGroupId());
            if (group != null) {
                vo.setGroupName(group.getGroupName());
            }
            User inviter = userMapper.findById(invite.getInviterId());
            if (inviter != null) {
                vo.setInviterName(inviter.getUsername());
                vo.setInviterAvatar(inviter.getAvatar());
            }
            voList.add(vo);
        }

        return new PageResult(pageInfo.getTotal(), voList);
    }

    @Override
    @Transactional
    public void acceptInvite(Long userId, Long inviteId) {
        GroupInvite invite = groupInviteMapper.findById(inviteId);
        if (invite == null) {
            throw new RuntimeException("邀请不存在");
        }
        if (!invite.getInvitedUserId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        if (invite.getStatus() != 0) {
            throw new RuntimeException("邀请已处理");
        }
        if (groupMemberMapper.exists(invite.getGroupId(), userId) > 0) {
            throw new RuntimeException("已在群中");
        }

        GroupMember member = new GroupMember();
        member.setGroupId(invite.getGroupId());
        member.setUserId(userId);
        member.setRole(0);
        groupMemberMapper.insert(member);
        groupInviteMapper.updateStatus(inviteId, 1);
    }

    @Override
    @Transactional
    public void rejectInvite(Long userId, Long inviteId) {
        GroupInvite invite = groupInviteMapper.findById(inviteId);
        if (invite == null) {
            throw new RuntimeException("邀请不存在");
        }
        if (!invite.getInvitedUserId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        if (invite.getStatus() != 0) {
            throw new RuntimeException("邀请已处理");
        }
        groupInviteMapper.updateStatus(inviteId, 2);
    }

    @Override
    public Integer getInviteCount(Long userId) {
        return groupInviteMapper.countPendingByUserId(userId);
    }

    @Override
    public boolean isGroupMember(Long groupId, Long userId) {
        return groupMemberMapper.exists(groupId, userId) > 0;
    }
}