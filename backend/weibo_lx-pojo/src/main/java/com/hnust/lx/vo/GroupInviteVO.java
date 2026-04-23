package com.hnust.lx.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInviteVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long inviteId;
    private Long groupId;
    private String groupName;
    private Long inviterId;
    private String inviterName;
    private String inviterAvatar;
    private LocalDateTime createTime;
}