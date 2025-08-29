package com.yren.study.custom.manage;

import com.yren.study.custom.queue.CustomResizableCapacityBlockingQueue;
import com.yren.study.custom.model.ThreadPoolStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ChenYu ren
 * @date 2025/8/27
 */

@Component
public class CustomDynamicThreadPoolManager {

    @Resource(name = "customJucThreadPoolExecutor")
    private ThreadPoolExecutor customThreadPool;

    /**
     * 动态调整线程池参数
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param queueCapacity 队列容量
     */
    public void adjustThreadPool(int corePoolSize, int maxPoolSize, int queueCapacity) {
        // 调整核心线程数
        if (corePoolSize != customThreadPool.getCorePoolSize()) {
            customThreadPool.setCorePoolSize(corePoolSize);
        }
        // 调整最大线程数
        if (maxPoolSize != customThreadPool.getMaximumPoolSize()) {
            customThreadPool.setMaximumPoolSize(maxPoolSize);
        }
        // 调整队列容量（需重置队列）
        if (queueCapacity != customThreadPool.getQueue().size()) {
            CustomResizableCapacityBlockingQueue<Runnable> queue =
                    (CustomResizableCapacityBlockingQueue<Runnable>) customThreadPool.getQueue();
            // 需要自定义可调整队列
            queue.setCapacity(queueCapacity);
        }
    }

    /**
     * 获取线程池状态
     */
    public ThreadPoolStatus getThreadPoolStatus() {
        return ThreadPoolStatus.fromExecutor(customThreadPool);
    }

}
