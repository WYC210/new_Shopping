package com.wyc.mapper;

import com.wyc.domain.po.MessageProcessRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageProcessRecordMapper {

    /**
     * 插入消息处理记录
     */
    int insert(MessageProcessRecord record);

    /**
     * 根据消息ID查询处理记录
     */
    MessageProcessRecord selectByMessageId(@Param("messageId") String messageId);

    /**
     * 更新处理状态
     */
    int updateStatus(@Param("messageId") String messageId, @Param("status") Integer status,
            @Param("remark") String remark);
}