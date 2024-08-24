package com.xxx.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxx.takeout.entity.SeckillGoods;

public interface SeckillService extends IService<SeckillGoods> {

    /**
     * 执行秒杀操作
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 秒杀成功返回true，失败返回false
     */
    boolean executeSeckill(Long userId, Long goodsId);
}