package com.wyc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    // 定义交换机名称
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // 定义队列名称
    public static final String ORDER_QUEUE = "order.queue";
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String DELAYED_ORDER_QUEUE = "delayed.order.queue";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";

    // 定义路由键
    public static final String ORDER_ROUTING_KEY = "order.create";
    public static final String PAYMENT_ROUTING_KEY = "payment.success";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";
    public static final String DELAYED_ORDER_ROUTING_KEY = "order.delayed";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        // 消息发送确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                logger.error("消息发送失败: {}", cause);
            }
        });

        // 消息发送失败回调
        rabbitTemplate.setReturnsCallback(returned -> {
            logger.error("消息发送失败: exchange={}, routingKey={}, replyCode={}, replyText={}, message={}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(),
                    returned.getReplyText(), new String(returned.getMessage().getBody()));
        });

        return rabbitTemplate;
    }

    /**
     * 订单交换机
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    /**
     * 支付交换机
     */
    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    /**
     * 通知交换机
     */
    @Bean
    public FanoutExchange notificationExchange() {
        return new FanoutExchange(NOTIFICATION_EXCHANGE);
    }

    /**
     * 订单队列
     */
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE).build();
    }

    /**
     * 支付队列
     */
    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE).build();
    }

    /**
     * 通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    /**
     * 延迟订单队列（用于订单超时取消）
     */
    @Bean
    public Queue delayedOrderQueue() {
        return QueueBuilder.durable(DELAYED_ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_CANCEL_ROUTING_KEY)
                .withArgument("x-message-ttl", 30 * 60 * 1000) // 30分钟
                .build();
    }

    /**
     * 订单取消队列（接收延迟队列的死信）
     */
    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE).build();
    }

    /**
     * 将订单队列绑定到订单交换机
     */
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }

    /**
     * 将支付队列绑定到支付交换机
     */
    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue()).to(paymentExchange()).with(PAYMENT_ROUTING_KEY);
    }

    /**
     * 将通知队列绑定到通知交换机
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange());
    }

    /**
     * 将延迟订单队列绑定到订单交换机
     */
    @Bean
    public Binding delayedOrderBinding() {
        return BindingBuilder.bind(delayedOrderQueue()).to(orderExchange()).with(DELAYED_ORDER_ROUTING_KEY);
    }

    /**
     * 将订单取消队列绑定到订单交换机
     */
    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue()).to(orderExchange()).with(ORDER_CANCEL_ROUTING_KEY);
    }
}