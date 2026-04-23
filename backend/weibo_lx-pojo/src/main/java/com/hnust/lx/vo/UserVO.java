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
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String username;
    private String avatar;
    private LocalDateTime createTime;
    private Integer status;
    private Integer userType;
    private String token;
    private Long totalLikes;
    private String bio;
}
