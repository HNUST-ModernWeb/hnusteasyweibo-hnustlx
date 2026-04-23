package com.hnust.lx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInvite implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long groupId;
    private Long invitedUserId;
    private Long inviterId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}