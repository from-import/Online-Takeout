package com.xxx.takeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxx.takeout.entity.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {
    // 如果需要自定义查询，可以在这里添加方法
}
