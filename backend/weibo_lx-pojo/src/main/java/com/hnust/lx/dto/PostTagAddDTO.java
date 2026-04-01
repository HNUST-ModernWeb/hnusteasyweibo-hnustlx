package com.hnust.lx.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostTagAddDTO {
    private Long postId;
    private Long userId;
    private List<Long> tagIds;
}
