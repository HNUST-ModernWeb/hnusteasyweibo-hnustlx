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
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String username;
    private String password;
    private String avatar;
    private LocalDateTime registerTime;
    private Integer userType;
    private Integer isDeleted;
}
