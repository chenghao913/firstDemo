package com.tencent.wxcloudrun.util;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}