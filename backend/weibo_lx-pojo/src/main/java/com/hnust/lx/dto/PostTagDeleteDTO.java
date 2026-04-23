package com.hnust.lx.dto;

import lombok.Data;

@Data
public class PostTagDeleteDTO {
    private Long postId;
    private Long tagId;
}
