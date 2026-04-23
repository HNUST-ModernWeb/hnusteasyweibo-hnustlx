package com.hnust.lx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMember implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long groupId;
    private Long userId;
    private Integer role;
    private LocalDateTime joinTime;
}