package com.xxx.takeout.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 队列名称
    public static final String SECKILL_QUEUE = "seckillQueue";

    // 交换机名称
    public static final String SECKILL_EXCHANGE = "seckillExchange";

    // 路由键
    public static final String SECKILL_ROUTING_KEY = "seckillRoutingKey";

    @Bean
    public Queue queue() {
        return new Queue(SECKILL_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(SECKILL_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(SECKILL_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 配置RabbitMQ发送消息确认机制
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.out.println("消息发送失败: " + cause);
            } else {
                System.out.println("消息发送成功");
            }
        });
        return rabbitTemplate;
    }
}
