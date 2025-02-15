package com.tencent.wxcloudrun.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ImageUploadRequest {

    @NotBlank(message = "关键字不能为空")
    private String keyword;

    @NotBlank(message = "文件ID不能为空")
    private String fileId;

    @NotBlank(message = "出处不能为空")
    private String bookSource;

    private String description;
}