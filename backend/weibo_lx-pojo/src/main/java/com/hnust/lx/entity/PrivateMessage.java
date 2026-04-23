package com.hnust.lx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime sendTime;
    private Integer isRead;
    private Integer isDeleted;
}