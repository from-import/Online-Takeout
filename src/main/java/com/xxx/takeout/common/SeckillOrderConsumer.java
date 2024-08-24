package com.xxx.takeout.common;

import com.xxx.takeout.config.RabbitMQConfig;
import com.xxx.takeout.entity.SeckillGoods;
import com.xxx.takeout.entity.SeckillOrder;
import com.xxx.takeout.entity.SeckillUser;
import com.xxx.takeout.mapper.SeckillGoodsMapper;
import com.xxx.takeout.mapper.SeckillOrderMapper;
import com.xxx.takeout.mapper.SeckillUserMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class SeckillOrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SeckillOrderConsumer.class);

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillUserMapper seckillUserMapper;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void handleSeckillOrder(String orderInfo) {
        try {
            // 解析订单信息
            logger.info("接收到秒杀订单: {}", orderInfo);

            String[] infoParts = orderInfo.split(",");
            if (infoParts.length < 2) {
                throw new IllegalArgumentException("Invalid orderInfo format: " + orderInfo);
            }

            Long userId = Long.parseLong(infoParts[0].split(":")[1]);
            Long goodsId = Long.parseLong(infoParts[1].split(":")[1]);
            int stockChange = -1; // 默认扣减 1 库存

            // 同步更新 MySQL 库存
            updateStockInDatabase(goodsId, stockChange);

            // 保存订单信息到数据库
            SeckillOrder order = new SeckillOrder();
            order.setUserId(userId);
            order.setGoodsId(goodsId);
            seckillOrderMapper.insert(order);

            // 保存用户秒杀信息到数据库
            SeckillUser seckillUser = new SeckillUser();
            seckillUser.setUserId(userId);
            seckillUser.setGoodsId(goodsId);
            seckillUser.setCreateTime(LocalDateTime.now());
            seckillUserMapper.insert(seckillUser);

            logger.info("订单信息已保存到数据库: userId={}, goodsId={}", userId, goodsId);

        } catch (Exception e) {
            logger.error("Error processing order: {}", orderInfo, e);
        }
    }

    private void updateStockInDatabase(Long goodsId, int stockChange) {
        try {
            SeckillGoods goods = seckillGoodsMapper.selectById(goodsId);
            if (goods != null) {
                goods.setStockCount(goods.getStockCount() + stockChange);
                seckillGoodsMapper.updateById(goods);
                logger.info("MySQL库存已更新: goodsId={}, stockChange={}", goodsId, stockChange);
            } else {
                logger.warn("Goods not found: goodsId={}", goodsId);
            }
        } catch (Exception e) {
            logger.error("Error updating stock in database for goodsId={}", goodsId, e);
        }
    }
}