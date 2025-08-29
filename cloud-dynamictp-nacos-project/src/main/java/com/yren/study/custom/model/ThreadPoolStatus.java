package com.yren.study.custom.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池状态信息对象
 * @author ChenYu ren
 * @date 2025/8/28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolStatus {
    
    /**
     * 核心线程数
     */
    private int corePoolSize;
    
    /**
     * 线程池最大线程数量
     */
    private int maximumPoolSize;
    
    /**
     * 活跃线程数
     */
    private int activeCount;
    
    /**
     * 队列当前元素数量
     */
    private int queueSize;
    
    /**
     * 队列剩余容量
     */
    private int queueRemainingCapacity;
    
    /**
     * 队列总长度
     */
    private int queueTotalCapacity;
    
    /**
     * 从ThreadPoolExecutor创建状态对象
     * @param executor 线程池执行器
     * @return 线程池状态对象
     */
    public static ThreadPoolStatus fromExecutor(ThreadPoolExecutor executor) {
        BlockingQueue<Runnable> queue = executor.getQueue();
        return ThreadPoolStatus.builder()
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .activeCount(executor.getActiveCount())
                .queueSize(queue.size())
                .queueRemainingCapacity(queue.remainingCapacity())
                .queueTotalCapacity(queue.remainingCapacity() + queue.size())
                .build();
    }
    
    /**
     * 获取线程池使用率
     * @return 使用率百分比
     */
    public double getThreadPoolUsageRate() {
        if (maximumPoolSize == 0) {
            return 0.0;
        }
        return (double) activeCount / maximumPoolSize * 100;
    }
    
    /**
     * 获取队列使用率
     * @return 队列使用率百分比
     */
    public double getQueueUsageRate() {
        if (queueTotalCapacity == 0) {
            return 0.0;
        }
        return (double) queueSize / queueTotalCapacity * 100;
    }
    
    @Override
    public String toString() {
        return String.format(
            "ThreadPoolStatus{核心线程数=%d, 最大线程数=%d, 活跃线程数=%d, " +
            "队列大小=%d, 队列剩余容量=%d, 队列总容量=%d, " +
            "线程池使用率=%.2f%%, 队列使用率=%.2f%%}",
            corePoolSize, maximumPoolSize, activeCount,
            queueSize, queueRemainingCapacity, queueTotalCapacity,
            getThreadPoolUsageRate(), getQueueUsageRate()
        );
    }

}