package com.tencent.wxcloudrun.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.wxcloudrun.model.Counter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CountersMapper  extends BaseMapper<Counter> {

  /**
     * 根据id获取计数器
     */
    Counter getCounter(@Param("id") Integer id);

    /**
     * 更新或插入计数
     */
    void upsertCount(Counter counter);

    /**
     * 清除计数
     */
    void clearCount(@Param("id") Integer id);
}
