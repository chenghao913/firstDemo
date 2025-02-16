package com.tencent.wxcloudrun.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

public class SecurityUtils {

    /**
     * 获取当前登录用户的 openid
     *
     * @return openid 或 null（未登录时）
     */
    public static String getCurrentOpenid() {
        // 从 SecurityContextHolder 获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 检查认证信息是否存在且已认证
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal != null) {
                String openid = principal.toString();
                // 确保 openid 不为空
                if (StringUtils.hasText(openid)) {
                    return openid;
                }
            }
        }
        return null;
    }

    /**
     * 检查用户是否已登录
     *
     * @return true 如果用户已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 检查当前用户是否有权限访问指定的 openid 资源
     *
     * @param targetOpenid 目标资源的 openid
     * @return true 如果当前用户有权限
     */
    public static boolean hasPermission(String targetOpenid) {
        String currentOpenid = getCurrentOpenid();
        return currentOpenid != null && currentOpenid.equals(targetOpenid);
    }
} 