package com.wyc.listener;

import com.rabbitmq.client.Channel;
import com.wyc.config.RabbitMQConfig;
import com.wyc.service.ITransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 支付消息监听器
 */
@Component
public class PaymentMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMessageListener.class);

    @Autowired
    private ITransactionService transactionService;

    /**
     * 处理支付成功消息
     */
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handlePaymentSuccessMessage(Map<String, Object> data, Message message, Channel channel)
            throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            logger.info("收到支付成功消息: {}", data);

            Long orderId = Long.valueOf(data.get("orderId").toString());
            Long paymentId = Long.valueOf(data.get("paymentId").toString());
            String thirdPartyTransactionId = data.containsKey("thirdPartyTransactionId")
                    ? data.get("thirdPartyTransactionId").toString()
                    : null;

            // 处理支付成功逻辑
            if (thirdPartyTransactionId != null) {
                transactionService.handlePaymentSuccess(paymentId, thirdPartyTransactionId);
            }

            // 确认消息
            channel.basicAck(deliveryTag, false);
            logger.info("支付成功消息处理完成: orderId={}, paymentId={}", orderId, paymentId);
        } catch (Exception e) {
            logger.error("处理支付成功消息失败", e);
            // 拒绝消息，并重新入队
            channel.basicNack(deliveryTag, false, true);
        }
    }
}