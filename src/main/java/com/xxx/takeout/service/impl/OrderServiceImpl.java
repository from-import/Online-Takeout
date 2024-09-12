package com.xxx.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxx.takeout.common.BaseContext;
import com.xxx.takeout.common.CustomException;
import com.xxx.takeout.entity.*;
import com.xxx.takeout.mapper.OrderMapper;
import com.xxx.takeout.service.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService; // 获取当前购物车数据

    @Autowired
    private UserService userService; // 获取用户数据

    @Autowired
    private AddressBookService addressBookService; // 获取用户地址

    @Autowired
    private RabbitTemplate rabbitTemplate;  // 注入 RabbitTemplate 对象

    @Autowired
    private OrderDetailService orderDetailService; // 插入订单详情数据

    @Transactional
    @Override
    public void submit(Orders orders) {
        // 三表操作
        // 1. 获取当前用户id
        Long userId = BaseContext.getCurrentId();

        // 2. 获取用户id对应的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空");
        }

        // 3.查询用户数据
        User user = userService.getById(userId);

        // 4.查询用户地址
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null){
            throw new CustomException("地址不存在");
        }

        // 5.遍历购物车数据，将购物车数据转换为订单详情数据
        long orderId = IdWorker.getId();//订单号

        // 原子操作，用于计算总金额，可以在多线程环境下保证数据的一致性
        AtomicInteger amount = new AtomicInteger(0); // 总金额

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            // 累加操作计算 总金额 = 单价 * 数量
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        // 6. order 插入数据（多条）
        this.save(orders);

        // 7. orderDetail 插入数据（多条）
        orderDetailService.saveBatch(orderDetails);

        // 8. 清空购物车
        shoppingCartService.remove(queryWrapper);

        // 将订单ID发送到订单超时队列
        // 死信队列用于处理超时的订单
        // 将订单ID发送到超时队列，初始TTL为10秒
        rabbitTemplate.convertAndSend("orderTimeoutExchange", "orderTimeoutRoutingKey", orderId,
                message -> {
                    message.getMessageProperties().setExpiration("10000");  // 第一个超时设定为10秒
                    return message;
                }
        );
        // 初始化订单的超时索引
        updateTimeoutIndex(orderId, 1);
        System.out.println("订单提交成功，并已发送到订单超时队列，订单ID：" + orders.getId());

    }

    // 删除超时订单的方法
    @Override
    public void removeOrderById(Long orderId) {
        // 1. 删除订单详情记录
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, orderId);
        orderDetailService.remove(detailWrapper);

        // 2. 删除订单记录
        this.removeById(orderId);
        System.out.println("订单及订单详情删除成功，订单ID: " + orderId);
    }

    @Override
    public boolean isOrderPaid(Long orderId) {
        Orders order = this.getById(orderId);
        return order != null && order.getStatus() == 1;  // 假设订单状态1表示已支付
    }

    // 模拟订单的超时检查索引存储，你可以将其存储在数据库中
    private Map<Long, Integer> orderTimeoutIndex = new HashMap<>();

    @Override
    public int getCurrentTimeoutIndex(Long orderId) {
        return orderTimeoutIndex.getOrDefault(orderId, 0);  // 获取订单当前的超时检查索引，默认从0开始
    }

    @Override
    public void updateTimeoutIndex(Long orderId, int index) {
        orderTimeoutIndex.put(orderId, index);  // 更新订单的超时检查索引
    }

}
