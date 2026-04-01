package com.hnust.lx.vo;

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
public class CommentVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long commentId;
    private Long postId;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private LocalDateTime createTime;
    private Integer status;
}
