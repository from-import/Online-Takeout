package com.xxx.takeout.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class SeckillOrder {

    @TableId
    private Long id;
    private Long userId;
    private Long orderId;
    private Long goodsId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
