package com.wyc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * SQL初始化工具类，用于项目启动时执行SQL脚本创建表
 */
@Component
public class SqlInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SqlInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 初始化消息处理记录表
            initMessageProcessRecordTable();
        } catch (Exception e) {
            logger.error("初始化数据库表失败", e);
        }
    }

    /**
     * 初始化消息处理记录表
     */
    private void initMessageProcessRecordTable() {
        try {
            // 读取SQL脚本
            String sql = readSqlScript("db/message_process_record.sql");

            // 执行SQL脚本
            jdbcTemplate.execute(sql);

            logger.info("消息处理记录表初始化成功");
        } catch (Exception e) {
            logger.error("初始化消息处理记录表失败", e);
        }
    }

    /**
     * 读取SQL脚本
     *
     * @param path 脚本路径
     * @return SQL脚本内容
     */
    private String readSqlScript(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}