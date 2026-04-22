package com.hnust.lx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long postId;
    private Long userId;
    private String content;
    private String images;
    private LocalDateTime postTime;
    private Integer commentCount;
    private Integer likeCount;
    private Integer isDeleted;
}
