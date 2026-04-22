package com.hnust.lx.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long postId;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private List<String> images;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long status;
}
