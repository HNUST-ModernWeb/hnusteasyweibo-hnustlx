package com.hnust.lx.dto;

import lombok.Data;

@Data
public class PrivateMessageSendDTO {
    private Long receiverId;
    private String content;
}