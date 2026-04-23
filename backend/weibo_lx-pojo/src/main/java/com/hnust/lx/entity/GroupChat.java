package com.hnust.lx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupChat implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long groupId;
    private String groupName;
    private Long creatorId;
    private String avatar;
    private LocalDateTime createTime;
    private Integer isDeleted;
}