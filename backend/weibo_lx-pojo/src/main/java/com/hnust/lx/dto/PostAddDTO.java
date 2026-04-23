package com.hnust.lx.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostAddDTO {
    private Long userId;
    private String content;
    private List<String> images;
}
