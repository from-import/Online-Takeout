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

    // 主队列名称
    public static final String SECKILL_QUEUE = "seckillQueue";
    public static final String SECKILL_EXCHANGE = "seckillExchange";
    public static final String SECKILL_ROUTING_KEY = "seckillRoutingKey";

    // 死信队列名称
    public static final String DEAD_LETTER_QUEUE = "deadLetterQueue";
    public static final String DEAD_LETTER_EXCHANGE = "deadLetterExchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "deadLetterRoutingKey";

    // 主队列配置
    @Bean
    public Queue queue() {
        return QueueBuilder.durable(SECKILL_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)  // 配置死信交换机
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)  // 配置死信路由键
                .build();
    }

    // 交换机配置
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(SECKILL_EXCHANGE);
    }

    // 绑定主队列到交换机
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(SECKILL_ROUTING_KEY);
    }

    // 死信队列配置
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);
    }

    // 死信交换机配置
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    // 绑定死信队列到死信交换机
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        // 配置RabbitMQ发送消息确认机制
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.out.println("消息发送失败: " + cause);
            } else {
                System.out.println("消息发送成功");
            }
        });

        // 配置ReturnCallback，用于处理无法路由的消息
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            System.out.println("消息无法路由: " + replyText);
            System.out.println("交换机: " + exchange + ", 路由键: " + routingKey + ", 消息: " + message);
        });
        return rabbitTemplate;
    }
}
