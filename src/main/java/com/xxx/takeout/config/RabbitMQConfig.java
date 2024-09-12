package com.xxx.takeout.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // 标记这是一个配置类，Spring 会自动扫描和加载
public class RabbitMQConfig {

    // 秒杀用的队列和交换机
    // 主队列名称，用于存储秒杀相关的消息
    public static final String SECKILL_QUEUE = "seckillQueue";
    // 秒杀相关的交换机名称
    public static final String SECKILL_EXCHANGE = "seckillExchange";
    // 秒杀相关的路由键
    public static final String SECKILL_ROUTING_KEY = "seckillRoutingKey";

    // 原来的基础死信队列和交换机
    // 死信队列名称，用于存储未成功处理的消息
    public static final String DEAD_LETTER_QUEUE = "deadLetterQueue";
    // 死信交换机名称
    public static final String DEAD_LETTER_EXCHANGE = "deadLetterExchange";
    // 死信队列的路由键
    public static final String DEAD_LETTER_ROUTING_KEY = "deadLetterRoutingKey";

    // RabbitMQ实现的订单超时管理
    // 超时订单队列，用于记录订单是否超时
    public static final String ORDER_TIMEOUT_QUEUE = "orderTimeoutQueue";
    // 处理超时订单的队列
    public static final String ORDER_TIMEOUT_PROCESS_QUEUE = "orderTimeoutProcessQueue";
    // 超时订单交换机
    public static final String ORDER_TIMEOUT_EXCHANGE = "orderTimeoutExchange";
    // 超时处理的路由键
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "orderTimeoutRoutingKey";

    // 独立的订单超时死信队列和死信交换机
    public static final String ORDER_TIMEOUT_DEAD_LETTER_QUEUE = "orderTimeoutDeadLetterQueue";
    public static final String ORDER_TIMEOUT_DEAD_LETTER_EXCHANGE = "orderTimeoutDeadLetterExchange";
    public static final String ORDER_TIMEOUT_DEAD_LETTER_ROUTING_KEY = "orderTimeoutDeadLetterRoutingKey";

    // 创建秒杀主队列的 Bean
    @Bean
    public Queue queue() {
        return QueueBuilder.durable(SECKILL_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)  // 使用原来的死信交换机
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)  // 使用原来的死信路由键
                .build();
    }

    // 创建秒杀相关的交换机
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(SECKILL_EXCHANGE);
    }

    // 绑定主队列到秒杀交换机
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(SECKILL_ROUTING_KEY);
    }

    // 创建原来的死信队列的 Bean
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);  // 保持队列持久化
    }

    // 创建原来的死信交换机的 Bean
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    // 绑定原来的死信队列到死信交换机
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }

    // 创建订单超时主队列的 Bean
    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_TIMEOUT_DEAD_LETTER_EXCHANGE)  // 独立的订单超时死信交换机
                .withArgument("x-dead-letter-routing-key", ORDER_TIMEOUT_DEAD_LETTER_ROUTING_KEY)  // 独立的订单超时死信路由键
                .withArgument("x-message-ttl", 60000)  // 设置消息存活时间，单位为毫秒
                .build();
    }

    // 创建处理订单超时的队列
    @Bean
    public Queue orderTimeoutProcessQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_PROCESS_QUEUE).build();
    }

    // 创建订单超时交换机
    @Bean
    public DirectExchange orderTimeoutExchange() {
        return new DirectExchange(ORDER_TIMEOUT_EXCHANGE);
    }

    // 绑定订单超时队列到订单超时交换机
    @Bean
    public Binding orderTimeoutBinding(Queue orderTimeoutQueue, DirectExchange orderTimeoutExchange) {
        return BindingBuilder.bind(orderTimeoutQueue).to(orderTimeoutExchange).with(ORDER_TIMEOUT_ROUTING_KEY);
    }

    // 创建订单超时独立的死信队列
    // 这个队列存放着超时的订单
    @Bean
    public Queue orderTimeoutDeadLetterQueue() {
        return new Queue(ORDER_TIMEOUT_DEAD_LETTER_QUEUE, true);  // 设置订单超时死信队列持久化
    }

    // 创建订单超时独立的死信交换机
    @Bean
    public DirectExchange orderTimeoutDeadLetterExchange() {
        return new DirectExchange(ORDER_TIMEOUT_DEAD_LETTER_EXCHANGE);
    }

    // 绑定订单超时独立的死信队列到死信交换机
    @Bean
    public Binding orderTimeoutDeadLetterBinding(Queue orderTimeoutDeadLetterQueue, DirectExchange orderTimeoutDeadLetterExchange) {
        return BindingBuilder.bind(orderTimeoutDeadLetterQueue).to(orderTimeoutDeadLetterExchange).with(ORDER_TIMEOUT_DEAD_LETTER_ROUTING_KEY);
    }

    // 创建 RabbitTemplate Bean，用于操作 RabbitMQ 的消息发送
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);  // 开启强制模式，保证消息不可达时返回给生产者

        // 配置发送确认机制（ConfirmCallback）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.out.println("消息发送失败: " + cause);  // 如果发送失败，打印原因
            } else {
                System.out.println("消息发送成功");  // 发送成功的反馈
            }
        });

        // 配置消息返回机制（ReturnCallback）
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            System.out.println("消息无法路由: " + replyText);  // 打印返回的错误信息
            System.out.println("交换机: " + exchange + ", 路由键: " + routingKey + ", 消息: " + message);  // 记录详细的无法路由的信息
        });
        return rabbitTemplate;
    }
}