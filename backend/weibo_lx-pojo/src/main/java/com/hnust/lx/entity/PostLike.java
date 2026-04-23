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
public class PostLike implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long likeId;
    private Long postId;
    private Long userId;
    private LocalDateTime likeTime;
}
