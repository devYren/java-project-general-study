package com.yren.study.custom.config;

import com.yren.study.custom.queue.CustomResizableCapacityBlockingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 * @author ChenYu ren
 * @date 2025/8/24
 */
@Configuration
public class CustomThreadPoolConfiguration {

    @Bean("customJucThreadPoolExecutor")
    public ThreadPoolExecutor customJucThreadPoolExecutor() {
        return new ThreadPoolExecutor(1,1,1, TimeUnit.SECONDS,new CustomResizableCapacityBlockingQueue<>(1));
    }

}


