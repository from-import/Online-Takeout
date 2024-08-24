package com.xxx.takeout.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_user")
public class SeckillUser {

    @TableId
    private Long id;
    private Long userId;
    private Long goodsId;
    private LocalDateTime createTime;
}
