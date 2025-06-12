package com.wyc.service.impl;

import com.wyc.domain.po.MessageProcessRecord;
import com.wyc.mapper.MessageProcessRecordMapper;
import com.wyc.service.IMessageProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 消息处理服务实现
 */
@Service
public class MessageProcessServiceImpl implements IMessageProcessService {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessServiceImpl.class);

    @Autowired
    private MessageProcessRecordMapper messageProcessRecordMapper;

    @Override
    public MessageProcessRecord recordMessageStart(String messageId, String messageType) {
        logger.info("记录消息处理开始: messageId={}, messageType={}", messageId, messageType);

        try {
            // 先查询是否已存在记录
            MessageProcessRecord record = messageProcessRecordMapper.selectByMessageId(messageId);
            if (record != null) {
                logger.info("消息已有处理记录: messageId={}, status={}", messageId, record.getStatus());
                return record;
            }

            // 创建新记录
            record = new MessageProcessRecord();
            record.setMessageId(messageId);
            record.setMessageType(messageType);
            record.setStatus(0); // 处理中
            record.setRemark("处理中");
            Date now = new Date();
            record.setCreatedAt(now);
            record.setUpdatedAt(now);

            try {
                messageProcessRecordMapper.insert(record);
                logger.info("消息处理记录创建成功: messageId={}", messageId);
            } catch (Exception e) {
                // 可能存在并发问题，再次查询
                logger.warn("插入消息处理记录失败，可能并发操作或表不存在: messageId={}, error={}", messageId, e.getMessage());
                try {
                    record = messageProcessRecordMapper.selectByMessageId(messageId);
                } catch (Exception ex) {
                    // 如果再次查询也失败，直接返回record对象
                    logger.warn("再次查询失败，可能是表不存在: {}", ex.getMessage());
                }
            }

            return record;
        } catch (Exception e) {
            // 如果表不存在，记录日志并返回一个临时对象
            logger.warn("记录消息处理开始失败，可能是表不存在: messageId={}, error={}", messageId, e.getMessage());
            MessageProcessRecord record = new MessageProcessRecord();
            record.setMessageId(messageId);
            record.setMessageType(messageType);
            record.setStatus(0);
            return record;
        }
    }

    @Override
    public boolean recordMessageSuccess(String messageId) {
        logger.info("记录消息处理成功: messageId={}", messageId);
        try {
            int rows = messageProcessRecordMapper.updateStatus(messageId, 1, "处理成功");
            return rows > 0;
        } catch (Exception e) {
            logger.warn("更新消息处理状态失败，可能是表不存在: messageId={}, error={}", messageId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean recordMessageFailure(String messageId, String errorMessage) {
        logger.info("记录消息处理失败: messageId={}, error={}", messageId, errorMessage);
        try {
            int rows = messageProcessRecordMapper.updateStatus(messageId, 2, errorMessage);
            return rows > 0;
        } catch (Exception e) {
            logger.warn("更新消息处理状态失败，可能是表不存在: messageId={}, error={}", messageId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isMessageProcessed(String messageId) {
        try {
            MessageProcessRecord record = messageProcessRecordMapper.selectByMessageId(messageId);
            if (record == null) {
                return false;
            }
            // 状态为1表示处理成功
            return record.getStatus() != null && record.getStatus() == 1;
        } catch (Exception e) {
            // 如果表不存在，记录日志并返回false
            logger.warn("查询消息处理记录失败，可能是表不存在: messageId={}, error={}", messageId, e.getMessage());
            return false;
        }
    }
}