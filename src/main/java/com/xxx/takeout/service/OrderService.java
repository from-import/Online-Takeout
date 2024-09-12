package com.xxx.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxx.takeout.common.R;
import com.xxx.takeout.entity.Orders;
import org.springframework.web.bind.annotation.RequestBody;

public interface OrderService  extends IService<Orders> {
    // 用户下单
    public void submit(Orders orders);

    // 删除超时订单的方法
    void removeOrderById(Long orderId);

    boolean isOrderPaid(Long orderId);

    int getCurrentTimeoutIndex(Long orderId);

    void updateTimeoutIndex(Long orderId, int index);
}
