package com.wyc.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VisitorMapper {

    /**
     * 根据指纹获取游客ID
     *
     * @param fingerprint 浏览器指纹
     * @return 游客ID
     */
    Long getVisitorIdByFingerprint(@Param("fingerprint") String fingerprint);

    /**
     * 创建新的游客记录
     *
     * @param fingerprint 浏览器指纹
     * @return 游客ID
     */
    Long createVisitor(@Param("fingerprint") String fingerprint);

    /**
     * 将用户ID绑定到游客指纹
     *
     * @param fingerprint 浏览器指纹
     * @param userId      用户ID
     */
    void bindUserIdToFingerprint(@Param("fingerprint") String fingerprint, @Param("userId") Long userId);
}