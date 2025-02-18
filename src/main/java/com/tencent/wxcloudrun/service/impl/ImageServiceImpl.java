package com.tencent.wxcloudrun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.dao.ImageMapper;
import com.tencent.wxcloudrun.dto.ImageSearchDTO;
import com.tencent.wxcloudrun.dto.ImageUploadRequest;
import com.tencent.wxcloudrun.model.Image;
import com.tencent.wxcloudrun.model.ImageVO;
import com.tencent.wxcloudrun.service.ImageService;
import com.tencent.wxcloudrun.service.WxCloudService;
import com.tencent.wxcloudrun.util.BusinessException;
import com.tencent.wxcloudrun.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {

    private final WxCloudService wxCloudService;
    private final ImageMapper imageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadImage(ImageUploadRequest request) {
        // 1. 获取当前用户
        String openid = SecurityUtils.getCurrentOpenid();
        if (openid == null) {
            throw new BusinessException("用户未登录");
        }

        try {
            // 2. 验证文件是否存在于云存储
            if (!wxCloudService.checkFileExists(request.getFileId())) {
                throw new BusinessException("文件不存在或已过期");
            }

            // 3. 保存图片信息
            Image image = new Image();
            image.setKeyword(request.getKeyword());
            image.setFileId(request.getFileId());
            image.setBookSource(request.getBookSource());
            image.setDescription(request.getDescription());
            image.setSubmitter(openid);
            image.setSubmitTime(LocalDateTime.now());

            save(image);

            log.info("图片上传成功: {}", image.getId());

        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new BusinessException("图片上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<ImageVO> searchByKeyword(String keyword) {
        // 1. 查询图片列表
        List<Image> images = lambdaQuery()
                .eq(StringUtils.isNotBlank(keyword), Image::getKeyword, keyword)
                .orderByDesc(Image::getSubmitTime)
                .list();

        return convertToImageVOList(images);
    }

    @Override
    public IPage<ImageVO> searchImages(ImageSearchDTO searchDTO, Page<Image> page) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Image> wrapper = Wrappers.<Image>lambdaQuery()
                .like(StringUtils.isNotBlank(searchDTO.getKeyword()),
                        Image::getKeyword, searchDTO.getKeyword())
                .eq(StringUtils.isNotBlank(searchDTO.getSubmitter()),
                        Image::getSubmitter, searchDTO.getSubmitter())
                .ge(searchDTO.getStartTime() != null,
                        Image::getSubmitTime, searchDTO.getStartTime())
                .le(searchDTO.getEndTime() != null,
                        Image::getSubmitTime, searchDTO.getEndTime())
                .orderByDesc(Image::getSubmitTime);

        // 2. 执行分页查询
        IPage<Image> imagePage = page(page, wrapper);

        // 3. 转换为VO
        return imagePage.convert(this::convertToImageVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(Long id) {
        // 1. 获取当前用户
        String openid = SecurityUtils.getCurrentOpenid();
//        String openid = "";
        if (openid == null) {
            throw new BusinessException("用户未登录");
        }

        try {
            // 2. 查询图片信息
            Image image = getById(id);
            if (image == null) {
                throw new BusinessException("图片不存在");
            }

            // 3. 权限检查
            if (!openid.equals(image.getSubmitter())) {
                throw new BusinessException("无权删除此图片");
            }

            // 4. 删除云存储文件
            wxCloudService.deleteFile(image.getFileId());

            // 5. 逻辑删除数据库记录
            image.setIsDeleted(true);
            image.setDeleteTime(LocalDateTime.now());
            updateById(image);

            log.info("图片删除成功: {}", id);

        } catch (Exception e) {
            log.error("删除图片失败", e);
            throw new BusinessException("删除图片失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteImages(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        String openid = SecurityUtils.getCurrentOpenid();
//        String openid = "";
        if (openid == null) {
            throw new BusinessException("用户未登录");
        }

        try {
            // 1. 查询图片信息
            List<Image> images = listByIds(ids);

            // 2. 权限检查
            boolean hasUnauthorized = images.stream()
                    .anyMatch(image -> !openid.equals(image.getSubmitter()));
            if (hasUnauthorized) {
                throw new BusinessException("包含无权删除的图片");
            }

            // 3. 删除云存储文件
            List<String> fileIds = images.stream()
                    .map(Image::getFileId)
                    .collect(Collectors.toList());

            for (String fileId : fileIds) {
                wxCloudService.deleteFile(fileId);
            }

            // 4. 批量逻辑删除
            LocalDateTime now = LocalDateTime.now();
            images.forEach(image -> {
                image.setIsDeleted(true);
                image.setDeleteTime(now);
            });

            updateBatchById(images);

            log.info("批量删除图片成功: {}", ids);

        } catch (Exception e) {
            log.error("批量删除图片失败", e);
            throw new BusinessException("批量删除图片失败：" + e.getMessage());
        }
    }

    @Override
    public ImageVO getImageDetail(Long id) {
        Image image = getById(id);
        if (image == null) {
            throw new BusinessException("图片不存在");
        }

        return convertToImageVO(image);
    }

    /**
     * 转换为ImageVO
     */
    private ImageVO convertToImageVO(Image image) {
        if (image == null) {
            return null;
        }

        ImageVO vo = new ImageVO();
        BeanUtils.copyProperties(image, vo);

        // 获取临时下载链接
        String downloadUrl = wxCloudService.getTempFileURL(image.getFileId());
        vo.setUrl(downloadUrl);

        return vo;
    }

    /**
     * 批量转换为ImageVO
     */
    private List<ImageVO> convertToImageVOList(List<Image> images) {
        if (CollectionUtils.isEmpty(images)) {
            return Collections.emptyList();
        }

        // 批量获取下载链接
        List<String> fileIds = images.stream()
                .map(Image::getFileId)
                .collect(Collectors.toList());

        Map<String, String> urlMap = wxCloudService.batchGetTempFileURL(fileIds);

        // 转换为VO
        return images.stream()
                .map(image -> {
                    ImageVO vo = new ImageVO();
                    BeanUtils.copyProperties(image, vo);
                    vo.setUrl(urlMap.getOrDefault(image.getFileId(), ""));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}