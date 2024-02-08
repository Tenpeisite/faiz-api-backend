package com.zhj.common.model.dto.file;

import lombok.Data;

import java.io.Serializable;

@Data
public class ImageVo implements Serializable {
    private static final long serialVersionUID = -4296258656223039373L;
    private String uid;
    private String name;
    private String status;
    private String url;
}