package com.tencent.wxcloudrun.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.wxcloudrun.dto.ImageSearchDTO;
import com.tencent.wxcloudrun.dto.ImageUploadRequest;
import com.tencent.wxcloudrun.model.Image;
import com.tencent.wxcloudrun.model.ImageVO;
import com.tencent.wxcloudrun.service.ImageService;
import com.tencent.wxcloudrun.util.BusinessException;
import com.tencent.wxcloudrun.util.Result;
import com.tencent.wxcloudrun.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;


@RestController
@RequestMapping("/api/images")
@Validated
@Slf4j
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public Result<Void> upload(@RequestBody @Valid ImageUploadRequest request) {
        try {
            imageService.uploadImage(request);
            return Result.success();
        } catch (BusinessException e) {
            log.error("上传图片失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页搜索图片
     */
    @GetMapping("/search")
    public Result<List<ImageVO>> search(@RequestParam String keyword) {
        try {
            List<ImageVO> result = imageService.searchByKeyword(keyword);
            return Result.success(result);
        } catch (BusinessException e) {
            log.error("搜索图片失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据关键字搜索图片
     */
    @GetMapping("/keyword/{keyword}")
    public Result<List<ImageVO>> searchByKeyword(@PathVariable String keyword) {
        try {
            List<ImageVO> images = imageService.searchByKeyword(keyword);
            return Result.success(images);
        } catch (BusinessException e) {
            log.error("关键字搜索图片失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取图片详情
     */
    @GetMapping("/detail")
    public Result<ImageVO> getDetail(@RequestParam Long id) {
        try {
            ImageVO image = imageService.getImageDetail(id);
            return Result.success(image);
        } catch (BusinessException e) {
            log.error("获取图片详情失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }


    /**
     * 删除单张图片
     */
    @PostMapping("/delete/{id}")  // 改为 POST 请求
    public Result<Void> delete(@PathVariable Long id) {
        try {
            imageService.deleteImage(id);
            return Result.success();
        } catch (BusinessException e) {
            log.error("删除图片失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除图片
     */
    @PostMapping("/batch/delete")  // 改为 POST 请求
    public Result<Void> batchDelete(@RequestBody @NotEmpty(message = "图片ID列表不能为空") List<Long> ids) {
        try {
            imageService.batchDeleteImages(ids);
            return Result.success();
        } catch (BusinessException e) {
            log.error("批量删除图片失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我上传的图片
     */
    @GetMapping("/my")
    public Result<IPage<ImageVO>> getMyImages(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size) {
        try {
            Page<Image> page = new Page<>(current, size);
            ImageSearchDTO searchDTO = new ImageSearchDTO();
            searchDTO.setSubmitter(SecurityUtils.getCurrentOpenid());
            searchDTO.setSubmitter("admin");
            IPage<ImageVO> result = imageService.searchImages(searchDTO, page);
            return Result.success(result);
        } catch (BusinessException e) {
            log.error("获取我的图片失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}