package com.hnust.lx.dto;

import lombok.Data;

@Data
public class PostUpdateDTO {
    private Long postId;
    private Long userId;
    private String content;
}
