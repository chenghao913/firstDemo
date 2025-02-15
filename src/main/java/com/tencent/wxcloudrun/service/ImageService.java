package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.dto.ImageSearchDTO;
import com.tencent.wxcloudrun.dto.ImageUploadRequest;
import com.tencent.wxcloudrun.model.Image;
import com.tencent.wxcloudrun.model.ImageVO;

import java.util.List;
public interface ImageService extends IService<Image> {
    // 上传图片
    void uploadImage(ImageUploadRequest request);

    // 根据关键字搜索图片
    List<ImageVO> searchByKeyword(String keyword);

    // 分页搜索图片
    IPage<ImageVO> searchImages(ImageSearchDTO searchDTO, Page<Image> page);

    // 删除图片
    void deleteImage(Long id);

    // 批量删除图片
    void batchDeleteImages(List<Long> ids);

    // 获取图片详情
    ImageVO getImageDetail(Long id);
}