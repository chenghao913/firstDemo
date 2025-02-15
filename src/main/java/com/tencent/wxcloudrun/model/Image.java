package com.tencent.wxcloudrun.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("images")
public class Image {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String keyword;

    @TableField("file_id")
    private String fileId;

    @TableField("book_source")
    private String bookSource;

    private String description;

    private String submitter;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableLogic  // 逻辑删除注解
    @TableField("is_deleted")
    private Boolean isDeleted;

    @TableField("delete_time")
    private LocalDateTime deleteTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}