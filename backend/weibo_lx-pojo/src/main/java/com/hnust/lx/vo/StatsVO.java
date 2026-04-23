package com.hnust.lx.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long userCount;
    private Long postCount;
    private Long commentCount;
    private Long tagCount;
}
