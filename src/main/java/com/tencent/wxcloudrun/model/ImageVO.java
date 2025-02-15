package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageVO {
    private Long id;
    private String keyword;
    private String url;
    private String bookSource;
    private String description;
    private String submitter;
    private LocalDateTime submitTime;
}