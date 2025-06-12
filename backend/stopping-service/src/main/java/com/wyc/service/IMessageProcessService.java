package com.wyc.service;

import com.wyc.domain.po.MessageProcessRecord;

/**
 * 消息处理服务接口
 */
public interface IMessageProcessService {

    /**
     * 记录消息处理开始
     * 
     * @param messageId   消息ID
     * @param messageType 消息类型
     * @return 处理记录
     */
    MessageProcessRecord recordMessageStart(String messageId, String messageType);

    /**
     * 记录消息处理成功
     * 
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean recordMessageSuccess(String messageId);

    /**
     * 记录消息处理失败
     * 
     * @param messageId    消息ID
     * @param errorMessage 错误信息
     * @return 是否成功
     */
    boolean recordMessageFailure(String messageId, String errorMessage);

    /**
     * 检查消息是否已处理
     * 
     * @param messageId 消息ID
     * @return 是否已处理
     */
    boolean isMessageProcessed(String messageId);
}