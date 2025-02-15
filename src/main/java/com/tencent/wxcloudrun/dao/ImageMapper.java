package com.tencent.wxcloudrun.dao;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tencent.wxcloudrun.dto.ImageSearchDTO;
import com.tencent.wxcloudrun.model.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImageMapper extends BaseMapper<Image> {

    // 自定义查询方法
    @Select("SELECT * FROM images WHERE " +
            "keyword LIKE CONCAT('%', #{keyword}, '%') " +
            "AND is_deleted = 0 " +
            "ORDER BY submit_time DESC")
    List<Image> searchByKeyword(@Param("keyword") String keyword);

    // 组合查询
    default IPage<Image> searchImages(Page<Image> page, ImageSearchDTO searchDTO) {
        return selectPage(page, new LambdaQueryWrapper<Image>()
                .like(StringUtils.isNotBlank(searchDTO.getKeyword()),
                        Image::getKeyword, searchDTO.getKeyword())
                .eq(StringUtils.isNotBlank(searchDTO.getSubmitter()),
                        Image::getSubmitter, searchDTO.getSubmitter())
                .ge(searchDTO.getStartTime() != null,
                        Image::getSubmitTime, searchDTO.getStartTime())
                .le(searchDTO.getEndTime() != null,
                        Image::getSubmitTime, searchDTO.getEndTime())
                .orderByDesc(Image::getSubmitTime));
    }
}