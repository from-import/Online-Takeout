package com.xxx.takeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxx.takeout.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {
    // 如果需要自定义查询，可以在这里添加方法
}
