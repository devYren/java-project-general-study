package com.yren.study.custom.parameters;

import com.yren.study.custom.manage.CustomDynamicThreadPoolManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 线程池配置中心
 * - 支持配置热更新
 * custom-dynamic:
 *   threadpool:
 *     core-size: 5
 *     max-size: 10
 *     queue-capacity: 100
 * @author ChenYu ren
 * @date 2025/8/28
 */

@Slf4j(topic = "CustomThreadPoolParameterConfig")
@Component
@RefreshScope
public class CustomThreadPoolParameterConfig {

    @Value("${custom-dynamic.threadpool.core-size}")
    private int coreSize = 5;

    @Value("${custom-dynamic.threadpool.max-size}")
    private int maxSize = 10;

    @Value("${custom-dynamic.threadpool.queue-capacity}")
    private int queueCapacity = 100;

    @Resource
    private CustomDynamicThreadPoolManager threadPoolManager;

    @PostConstruct
    public void init() {
        // 初始化线程池参数
        threadPoolManager.adjustThreadPool(coreSize, maxSize, queueCapacity);
    }

}
