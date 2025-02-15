package com.tencent.wxcloudrun.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.hutool.http.HttpUtil;
import com.tencent.wxcloudrun.util.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WxCloudService {

    @Autowired
    private WxMaService wxMaService;

    @Value("${wx.miniapp.env}")
    private String env;

    /**
     * 检查文件是否存在
     */
    public boolean checkFileExists(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return false;
        }

        try {
            String accessToken = wxMaService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/tcb/batchdownloadfile?access_token=%s", accessToken);

            // 构建请求参数
            JSONObject fileObj = new JSONObject();
            fileObj.put("fileid", fileId);
            fileObj.put("max_age", 7200);

            JSONObject params = new JSONObject();
            params.put("env", env);
            params.put("file_list", Collections.singletonList(fileObj));

            // 发送请求
            String result = HttpUtil.post(url, params.toJSONString());

            // 解析响应
            JSONObject response = JSON.parseObject(result);

            if (response.getIntValue("errcode") == 0) {
                JSONArray fileList = response.getJSONArray("file_list");
                if (!fileList.isEmpty()) {
                    JSONObject fileInfo = fileList.getJSONObject(0);
                    // 检查文件状态
                    Integer status = fileInfo.getInteger("status");
                    String downloadUrl = fileInfo.getString("download_url");
                    return status == 0 && StringUtils.isNotBlank(downloadUrl);
                }
            }

            log.error("检查文件存在失败: {}", response.getString("errmsg"));
            return false;

        } catch (Exception e) {
            log.error("检查文件存在失败", e);
            return false;
        }
    }


    /**
     * 获取文件临时下载链接
     */
    public String getTempFileURL(String fileId) {
        try {
            String accessToken = wxMaService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/tcb/batchdownloadfile?access_token=%s", accessToken);

            // 构建请求参数
            JSONObject fileObj = new JSONObject();
            fileObj.put("fileid", fileId);
            fileObj.put("max_age", 7200);

            JSONObject params = new JSONObject();
            params.put("env", env);
            params.put("file_list", Collections.singletonList(fileObj));

            // 发送请求
            String result = HttpUtil.post(url, params.toJSONString());

            // 解析响应
            JSONObject response = JSON.parseObject(result);

            if (response.getIntValue("errcode") == 0) {
                JSONArray fileList = response.getJSONArray("file_list");
                if (!fileList.isEmpty()) {
                    return fileList.getJSONObject(0).getString("download_url");
                }
            }

            log.error("获取文件下载链接失败: {}", response.getString("errmsg"));
            return null;

        } catch (Exception e) {
            log.error("获取文件下载链接失败", e);
            return null;
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileId) {
        try {
            String accessToken = wxMaService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/tcb/batchdeletefile?access_token=%s", accessToken);

            // 构建请求参数
            JSONObject params = new JSONObject();
            params.put("env", env);
            params.put("fileid_list", Collections.singletonList(fileId));

            // 发送请求
            String result = HttpUtil.post(url, params.toJSONString());

            // 解析响应
            JSONObject response = JSON.parseObject(result);

            if (response.getIntValue("errcode") != 0) {
                throw new BusinessException("删除文件失败：" + response.getString("errmsg"));
            }

            log.info("文件删除成功: {}", fileId);

        } catch (Exception e) {
            log.error("删除文件失败", e);
            throw new BusinessException("删除文件失败：" + e.getMessage());
        }
    }

    /**
     * 批量获取文件临时下载链接
     */
    public Map<String, String> batchGetTempFileURL(List<String> fileIds) {
        try {
            String accessToken = wxMaService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/tcb/batchdownloadfile?access_token=%s", accessToken);

            // 构建文件列表
            List<JSONObject> fileList = fileIds.stream()
                    .map(fileId -> {
                        JSONObject fileObj = new JSONObject();
                        fileObj.put("fileid", fileId);
                        fileObj.put("max_age", 7200);
                        return fileObj;
                    })
                    .collect(Collectors.toList());

            // 构建请求参数
            JSONObject params = new JSONObject();
            params.put("env", env);
            params.put("file_list", fileList);

            // 发送请求
            String result = HttpUtil.post(url, params.toJSONString());

            // 解析响应
            JSONObject response = JSON.parseObject(result);

            if (response.getIntValue("errcode") == 0) {
                Map<String, String> urlMap = new HashMap<>();
                JSONArray files = response.getJSONArray("file_list");

                for (int i = 0; i < files.size(); i++) {
                    JSONObject file = files.getJSONObject(i);
                    String fileId = file.getString("fileid");
                    String downloadUrl = file.getString("download_url");
                    if (StringUtils.isNotBlank(downloadUrl)) {
                        urlMap.put(fileId, downloadUrl);
                    }
                }

                return urlMap;
            }

            log.error("批量获取文件下载链接失败: {}", response.getString("errmsg"));
            return Collections.emptyMap();

        } catch (Exception e) {
            log.error("批量获取文件下载链接失败", e);
            return Collections.emptyMap();
        }
    }
}
