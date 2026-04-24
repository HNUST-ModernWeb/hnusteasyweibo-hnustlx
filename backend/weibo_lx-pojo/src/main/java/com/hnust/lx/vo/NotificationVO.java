package com.hnust.lx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class NotificationVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String type;
    private Object data;
    private Long timestamp;
}