package com.hnust.lx.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberVO {
    private Long userId;
    private String username;
    private String avatar;
    private Integer role;
    private LocalDateTime joinTime;
}