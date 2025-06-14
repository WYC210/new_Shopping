package com.wyc.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 访客数据访问层
 *
 * @author wyc
 */
@Mapper
public interface VisitorMapper {

    /**
     * 如果访客不存在，则插入新访客记录
     *
     * @param fingerprint 浏览器指纹
     * @return 影响行数
     */
    int insertVisitorIfNotExists(@Param("fingerprint") String fingerprint);

    /**
     * 更新访客关联的用户ID
     *
     * @param fingerprint 浏览器指纹
     * @param userId      用户ID
     * @return 影响行数
     */
    int updateVisitorUserId(@Param("fingerprint") String fingerprint, @Param("userId") Long userId);

    /**
     * 插入用户浏览记录
     *
     * @param userId      用户ID
     * @param productId   商品ID
     * @param productName 商品名称
     * @return 影响行数
     */
    int insertUserBrowsingRecord(@Param("userId") Long userId, @Param("productId") Long productId,
            @Param("productName") String productName);

    /**
     * 获取访客ID
     *
     * @param fingerprint 浏览器指纹
     * @return 访客ID
     */
    Long getVisitorIdByFingerprint(@Param("fingerprint") String fingerprint);

    /**
     * 检查访客是否存在
     *
     * @param fingerprint 浏览器指纹
     * @return 是否存在
     */
    boolean checkVisitorExists(@Param("fingerprint") String fingerprint);

    /**
     * 将用户ID绑定到访客指纹
     * 
     * @param fingerprint 浏览器指纹
     * @param userId      用户ID
     */
    void bindUserIdToFingerprint(@Param("fingerprint") String fingerprint, @Param("userId") Long userId);
}