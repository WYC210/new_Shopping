package com.wyc.service;

import java.util.List;

/**
 * 访客服务接口
 * 处理未登录用户的浏览记录
 *
 * @author wyc
 */
public interface IVisitorService {

    /**
     * 记录访客浏览商品
     *
     * @param fingerprint 浏览器指纹
     * @param productId   商品ID
     * @param productName 商品名称
     * @return 是否记录成功
     */
    boolean recordVisitorBrowsing(String fingerprint, Long productId, String productName);

    /**
     * 获取访客浏览历史
     *
     * @param fingerprint 浏览器指纹
     * @param limit       限制数量
     * @return 浏览历史列表
     */
    List<Object> getVisitorBrowsingHistory(String fingerprint, int limit);

    /**
     * 将访客浏览记录关联到用户
     *
     * @param fingerprint 浏览器指纹
     * @param userId      用户ID
     * @return 是否关联成功
     */
    boolean associateVisitorToUser(String fingerprint, Long userId);

    /**
     * 清除访客浏览记录
     *
     * @param fingerprint 浏览器指纹
     * @return 是否清除成功
     */
    boolean clearVisitorBrowsingHistory(String fingerprint);
}