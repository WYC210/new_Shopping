package com.wyc.mapper;

import com.wyc.domain.vo.BrowsingRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VisitorBrowsingRecordMapper {

    /**
     * 获取用户浏览历史
     *
     * @param userId 用户ID
     * @return 浏览历史列表
     */
    List<BrowsingRecordVO> getBrowsingHistory(@Param("userId") Long userId);

    /**
     * 记录浏览历史
     *
     * @param visitorId   游客ID
     * @param productId   商品ID
     * @param productName 商品名称
     */
    void recordBrowsing(@Param("visitorId") Long visitorId,
            @Param("productId") Long productId,
            @Param("productName") String productName);

    void insertUserBrowsingRecord(Long userId, Long productId, String productName, java.util.Date viewedAt);
}