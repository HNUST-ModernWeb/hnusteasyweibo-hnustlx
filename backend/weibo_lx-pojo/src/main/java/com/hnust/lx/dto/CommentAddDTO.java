package com.hnust.lx.dto;

import lombok.Data;

@Data
public class CommentAddDTO {
    private Long userId;
    private Long postId;
    private String content;
}
