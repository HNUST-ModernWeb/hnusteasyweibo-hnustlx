package com.hnust.lx.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatVO {
    private Long groupId;
    private String groupName;
    private String avatar;
    private Long memberCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}