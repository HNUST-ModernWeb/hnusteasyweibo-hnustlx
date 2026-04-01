package com.hnust.lx.dto;

import lombok.Data;

@Data
public class UserStatusDTO {
    private Long userId;
    private Integer isDeleted;
}
