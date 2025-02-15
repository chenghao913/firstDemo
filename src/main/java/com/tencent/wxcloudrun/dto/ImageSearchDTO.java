package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageSearchDTO {
    private String keyword;
    private String submitter;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}