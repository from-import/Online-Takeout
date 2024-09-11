package com.xxx.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxx.takeout.entity.Orders;
import com.xxx.takeout.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class CancelOrderJobHandler {

    @Autowired
    private OrderService orderService;

    @XxlJob("cancelOrderJobHandler")
    public void execute() throws Exception {
        log.info("开始执行定时取消超时订单任务");

        // 定义超时的时间，例如30分钟
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);

        // 查询超时且未支付的订单
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, 2); // 状态2表示待支付
        queryWrapper.lt(Orders::getOrderTime, timeoutThreshold);

        List<Orders> overdueOrders = orderService.list(queryWrapper);

        // 批量取消超时订单
        if (!overdueOrders.isEmpty()) {
            overdueOrders.forEach(order -> {
                order.setStatus(4); // 4表示订单已取消
                orderService.updateById(order);
                log.info("订单 {} 已被取消", order.getId());
            });
        }

        log.info("定时取消超时订单任务执行完毕");
    }
}