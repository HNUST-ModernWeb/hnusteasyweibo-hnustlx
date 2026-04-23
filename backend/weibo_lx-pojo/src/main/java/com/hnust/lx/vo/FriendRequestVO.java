package com.hnust.lx.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestVO {
    private Long requestId;
    private Long fromUserId;
    private String fromUsername;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
}