package com.wyc.listener;

import com.wyc.domain.dto.ProductDetailDTO;
import com.wyc.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 商品变更监听器
 * 监听商品变更事件，更新缓存和其他相关服务
 *
 * @author wyc
 */
@Slf4j
@Component
public class ProductChangeListener {

    @Autowired
    private IProductService productService;

    /**
     * 监听商品创建事件
     *
     * @param productId 商品ID
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "product.create.queue"), exchange = @Exchange(name = "product.exchange", type = ExchangeTypes.TOPIC), key = "product.create"))
    public void listenProductCreate(Long productId) {
        log.info("接收到商品创建事件: productId={}", productId);

        try {
            // 获取商品详情
            ProductDetailDTO product = productService.getProductDetail(productId);
            if (product == null) {
                log.error("商品不存在: productId={}", productId);
                return;
            }

            // 更新缓存
            productService.refreshProductCache(productId);
            log.info("商品创建事件处理完成: productId={}", productId);
        } catch (Exception e) {
            log.error("处理商品创建事件异常: productId={}", productId, e);
        }
    }

    /**
     * 监听商品更新事件
     *
     * @param productId 商品ID
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "product.update.queue"), exchange = @Exchange(name = "product.exchange", type = ExchangeTypes.TOPIC), key = "product.update"))
    public void listenProductUpdate(Long productId) {
        log.info("接收到商品更新事件: productId={}", productId);

        try {
            // 获取商品详情
            ProductDetailDTO product = productService.getProductDetail(productId);
            if (product == null) {
                log.error("商品不存在: productId={}", productId);
                return;
            }

            // 更新缓存
            productService.refreshProductCache(productId);
            log.info("商品更新事件处理完成: productId={}", productId);
        } catch (Exception e) {
            log.error("处理商品更新事件异常: productId={}", productId, e);
        }
    }

    /**
     * 监听商品删除事件
     *
     * @param productId 商品ID
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "product.delete.queue"), exchange = @Exchange(name = "product.exchange", type = ExchangeTypes.TOPIC), key = "product.delete"))
    public void listenProductDelete(Long productId) {
        log.info("接收到商品删除事件: productId={}", productId);

        try {
            // 清除缓存
            productService.removeProductCache(productId);
            log.info("商品删除事件处理完成: productId={}", productId);
        } catch (Exception e) {
            log.error("处理商品删除事件异常: productId={}", productId, e);
        }
    }
}