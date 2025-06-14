package com.wyc.config;

import com.wyc.service.ISearchService;
import com.wyc.service.impl.SearchServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 搜索服务配置
 * 提供基于数据库的搜索服务
 *
 * @author wyc
 */
@Configuration
public class MockSearchServiceConfig {
    private static final Logger logger = LoggerFactory.getLogger(MockSearchServiceConfig.class);

    /**
     * 提供数据库搜索服务实现
     */
    @Bean
    public ISearchService searchService() {
        logger.info("初始化数据库搜索服务");
        return new SearchServiceImpl();
    }
}