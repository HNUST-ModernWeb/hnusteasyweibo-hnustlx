package com.hnust.lx.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostUpdateDTO {
    private Long postId;
    private Long userId;
    private String content;
    private List<String> images;
}
