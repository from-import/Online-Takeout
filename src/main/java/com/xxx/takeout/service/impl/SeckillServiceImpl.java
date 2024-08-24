package com.xxx.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.takeout.config.RabbitMQConfig;
import com.xxx.takeout.entity.SeckillGoods;
import com.xxx.takeout.mapper.SeckillGoodsMapper;
import com.xxx.takeout.service.SeckillService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements SeckillService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    private static final String STOCK_PREFIX = "seckill:stock:";
    private static final String EMPTY_STOCK = "0"; // 用于表示无库存或商品不存在
    private static final long EMPTY_STOCK_TTL = 60; // 缓存空值的过期时间（秒）

    /**
     * 项目启动时，将秒杀商品库存信息加载到 Redis 缓存中
     */
    @PostConstruct
    public void preheatCache() {
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(new QueryWrapper<>());

        for (SeckillGoods goods : seckillGoodsList) {
            String stockKey = STOCK_PREFIX + goods.getId();
            redisTemplate.opsForValue().set(stockKey, String.valueOf(goods.getStockCount()));
        }

        System.out.println("商品库存信息已加载到Redis缓存中");

        Set<String> stockKeys = redisTemplate.keys(STOCK_PREFIX + "*");
        if (stockKeys != null && !stockKeys.isEmpty()) {
            System.out.println("加载到 Redis 中的商品库存信息如下：");
            for (String key : stockKeys) {
                String stock = redisTemplate.opsForValue().get(key);
                System.out.println("商品ID: " + key.replace(STOCK_PREFIX, "") + " 库存: " + stock);
            }
        } else {
            System.out.println("未找到任何库存信息在 Redis 中。");
        }
    }

    /**
     * 执行秒杀操作
     * @param userId 用户ID
     * @param goodsId 商品ID
     * 使用分布式锁的时候，如果主节点宕机，自动选举的master也获得了一个锁，怎么办？
     *                答:使用RedLock(红锁),不仅仅在一个redis实例创建锁，
     *                而是在多个redis实例创建锁（0.5 * N + 1)，只有大部分的redis实例创建锁成功，才算成功。
     *                这种设计可以保证Redis主从一致性，但是有性能问题
     *                非要保证强一致性，可以采用Zookeeper
     *
     */
    @Override
    public boolean executeSeckill(Long userId, Long goodsId) {
        String seckillLockKey = "seckill:lock:" + goodsId + ":" + userId;
        RLock seckillLock = redissonClient.getLock(seckillLockKey);
        try {
            // 加锁，防止并发问题，使用看门狗机制
            seckillLock.lock();

            // 检查是否已经秒杀过
            String seckillKey = "seckill:user:" + userId + ":goods:" + goodsId;
            if (redisTemplate.hasKey(seckillKey)) {
                return false;  // 已经秒杀过
            }

            // 检查库存
            String stockKey = STOCK_PREFIX + goodsId;
            String stockStr = redisTemplate.opsForValue().get(stockKey);

            if (stockStr == null) {
                // 缓存中没有商品时，使用单独的分布式锁来查询数据库并写入缓存
                String dbLockKey = "dbLock:" + goodsId;
                RLock dbLock = redissonClient.getLock(dbLockKey);
                try {
                    dbLock.lock(10, TimeUnit.SECONDS);

                    // 再次检查缓存，防止其他线程已经更新缓存
                    stockStr = redisTemplate.opsForValue().get(stockKey);
                    if (stockStr == null) {
                        // 如果 Redis 中没有该商品的库存信息，则查询数据库
                        SeckillGoods goods = seckillGoodsMapper.selectById(goodsId);
                        if (goods == null) {
                            // 如果数据库中也没有该商品，则缓存一个空值并设置短 TTL, 防止缓存穿透
                            redisTemplate.opsForValue().set(stockKey, EMPTY_STOCK, EMPTY_STOCK_TTL, TimeUnit.SECONDS);
                            return false;  // 商品不存在
                        } else {
                            // 如果商品存在，则将库存加载到 Redis
                            stockStr = String.valueOf(goods.getStockCount());
                            redisTemplate.opsForValue().set(stockKey, stockStr);
                        }
                    }
                } finally {
                    dbLock.unlock();  // 释放数据库锁
                }
            }

            // 使用 Lua 脚本实现库存原子性扣减和用户秒杀记录的操作
            String luaScript = "local stock = tonumber(redis.call('get', KEYS[1])) " +
                    "if not stock or stock <= 0 then return 0 end " +  // 库存不足
                    "if redis.call('exists', KEYS[2]) == 1 then return -1 end " +  // 已经秒杀过
                    "redis.call('decrby', KEYS[1], 1) " +  // 扣减库存
                    "redis.call('set', KEYS[2], 1) " +  // 记录秒杀用户
                    "return 1";  // 秒杀成功

            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(luaScript);
            script.setResultType(Long.class);

            // 使用 Arrays.asList 来创建键列表
            List<String> keys = Arrays.asList(stockKey, seckillKey);

            Long result = redisTemplate.execute(script, keys);

            if (result != null && result == 1) {
                // 异步发送订单消息
                sendOrderToQueue(userId, goodsId);
                return true;  // 秒杀成功
            } else if (result != null && result == -1) {
                return false;  // 已经秒杀过
            } else {
                return false;  // 库存不足
            }
        } finally {
            seckillLock.unlock(); // 确保释放锁
        }
    }

    /**
     * 发送订单消息至队列
     * @param userId 用户ID
     * @param goodsId 商品ID
     */
    private void sendOrderToQueue(Long userId, Long goodsId) {
        // 构建秒杀订单信息
        String orderInfo = "userId:" + userId + ",goodsId:" + goodsId;
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE, RabbitMQConfig.SECKILL_ROUTING_KEY, orderInfo);
        System.out.println("订单消息发送至队列: " + orderInfo);
    }
}