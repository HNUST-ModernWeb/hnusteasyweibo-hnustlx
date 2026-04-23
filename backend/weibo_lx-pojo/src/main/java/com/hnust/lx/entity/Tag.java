package com.hnust.lx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long tagId;
    private String tagName;
    private Integer isDeleted;
}
