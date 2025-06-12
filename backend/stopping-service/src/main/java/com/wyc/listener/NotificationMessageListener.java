package com.wyc.listener;

import com.rabbitmq.client.Channel;
import com.wyc.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 通知消息监听器
 */
@Component
public class NotificationMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationMessageListener.class);

    /**
     * 处理通知消息
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationMessage(Map<String, Object> data, Message message, Channel channel)
            throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            logger.info("收到通知消息: {}", data);

            Long userId = Long.valueOf(data.get("userId").toString());
            String title = data.get("title").toString();
            String content = data.get("content").toString();

            // 处理通知逻辑，如发送短信、邮件、推送等
            sendNotification(userId, title, content);

            // 确认消息
            channel.basicAck(deliveryTag, false);
            logger.info("通知消息处理完成: userId={}, title={}", userId, title);
        } catch (Exception e) {
            logger.error("处理通知消息失败", e);
            // 拒绝消息，并重新入队
            channel.basicNack(deliveryTag, false, true);
        }
    }

    /**
     * 发送通知（模拟实现）
     */
    private void sendNotification(Long userId, String title, String content) {
        // 这里可以实现具体的通知发送逻辑，如短信、邮件、推送等
        logger.info("发送通知: userId={}, title={}, content={}", userId, title, content);
        // 模拟发送延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}