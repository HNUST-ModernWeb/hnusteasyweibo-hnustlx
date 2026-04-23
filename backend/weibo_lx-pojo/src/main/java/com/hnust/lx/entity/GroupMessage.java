package com.hnust.lx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private Long groupId;
    private Long senderId;
    private String content;
    private LocalDateTime sendTime;
}