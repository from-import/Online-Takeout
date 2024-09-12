package com.xxx.takeout.common;

import com.xxx.takeout.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderStatusCheckConsumer {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 定义超时检查的时间段
    private static final List<Long> TIMEOUTS = new ArrayList<Long>() {{
        add(10000L); // 10秒
        add(30000L); // 30秒
        add(60000L); // 60秒
        add(100000L); // 100秒
    }};

    // 监听死信队列的超时订单
    @RabbitListener(queues = "orderTimeoutQueue")  // 监听订单超时队列
    public void handleTimeoutOrder(Long orderId) {
        try {
            // 检查订单状态
            boolean isPaid = orderService.isOrderPaid(orderId);  // 假设这个方法检查订单是否已支付

            if (isPaid) {
                System.out.println("订单已支付，无需进一步操作，订单ID: " + orderId);
            } else {
                System.out.println("订单未支付，订单ID: " + orderId);
                // 如果订单未支付，发送到下一个超时队列

                int nextIndex = orderService.getCurrentTimeoutIndex(orderId);
                if (nextIndex < TIMEOUTS.size()) {
                    Long nextTimeout = TIMEOUTS.get(nextIndex);

                    // 设置下一个超时
                    rabbitTemplate.convertAndSend(
                            "orderTimeoutExchange",
                            "orderTimeoutRoutingKey",
                            orderId,
                            message -> {
                                message.getMessageProperties(   ).setExpiration(nextTimeout.toString()); // 设置下一个TTL
                                return message;
                            }
                    );
                    // 更新订单的超时检查索引
                    orderService.updateTimeoutIndex(orderId, nextIndex + 1);
                } else {
                    System.out.println("所有超时时间已检查完毕，订单仍未支付，订单ID: " + orderId);
                    // 你可以选择在此删除未支付的订单，或执行其他操作
                }
            }
        } catch (Exception e) {
            System.err.println("处理超时订单失败，订单ID: " + orderId + "，原因: " + e.getMessage());
        }
    }
}