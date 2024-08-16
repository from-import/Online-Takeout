package com.xxx.takeout.controller;

import com.xxx.takeout.common.R;
import com.xxx.takeout.entity.Orders;
import com.xxx.takeout.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("提交订单：{}", orders);

        // 具体实现细节封装到service层
        orderService.submit(orders);
        return R.success("提交订单成功");
    }
}
