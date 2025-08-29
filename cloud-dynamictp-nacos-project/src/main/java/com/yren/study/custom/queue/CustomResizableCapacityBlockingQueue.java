package com.yren.study.custom.queue;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 自定义可调整队列容量的阻塞队列
 * 通过反射修改LinkedBlockingQueue的内部capacity字段实现动态调整
 * @author ChenYu ren
 * @date 2025/8/27
 */
public class CustomResizableCapacityBlockingQueue<E> extends LinkedBlockingQueue<E> {

    /**
     * -- GETTER --
     *  获取当前容量
     * @return 当前队列容量
     */
    private volatile int currentCapacity;
    private static final Field CAPACITY_FIELD;
    private static final Field PUT_LOCK_FIELD;
    private static final Field TAKE_LOCK_FIELD;
    
    static {
        try {
            CAPACITY_FIELD = LinkedBlockingQueue.class.getDeclaredField("capacity");
            CAPACITY_FIELD.setAccessible(true);
            
            PUT_LOCK_FIELD = LinkedBlockingQueue.class.getDeclaredField("putLock");
            PUT_LOCK_FIELD.setAccessible(true);
            
            TAKE_LOCK_FIELD = LinkedBlockingQueue.class.getDeclaredField("takeLock");
            TAKE_LOCK_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("初始化反射字段失败", e);
        }
    }

    public CustomResizableCapacityBlockingQueue(int capacity) {
        super(capacity);
        this.currentCapacity = capacity;
    }

    /**
     * 动态调整队列容量
     * @param newCapacity 新容量
     */
    public synchronized void setCapacity(int newCapacity) {
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("队列容量必须大于0");
        }
        
        if (newCapacity == currentCapacity) {
            return; // 容量未变化，直接返回
        }
        
        try {
            // 获取读写锁，确保线程安全
            ReentrantLock putLock = (ReentrantLock) PUT_LOCK_FIELD.get(this);
            ReentrantLock takeLock = (ReentrantLock) TAKE_LOCK_FIELD.get(this);
            
            // 按顺序获取锁，避免死锁
            putLock.lock();
            try {
                takeLock.lock();
                try {
                    int currentSize = size();
                    
                    // 检查缩容时是否会丢失数据
                    if (newCapacity < currentSize) {
                        throw new IllegalArgumentException(
                            String.format("新容量(%d)小于当前队列大小(%d)，缩容会导致数据丢失", 
                                newCapacity, currentSize));
                    }
                    
                    // 通过反射修改capacity字段
                    CAPACITY_FIELD.set(this, newCapacity);
                    this.currentCapacity = newCapacity;
                    
                    // 如果是扩容，唤醒等待的生产者线程
                    if (newCapacity > currentCapacity) {
                        // 通知等待的put操作
                        signalNotFull();
                    }
                    
                } finally {
                    takeLock.unlock();
                }
            } finally {
                putLock.unlock();
            }
            
        } catch (Exception e) {
            throw new RuntimeException("动态调整队列容量失败", e);
        }
    }
    
    /**
     * 唤醒等待队列不满的线程
     */
    private void signalNotFull() {
        try {
            ReentrantLock putLock = (ReentrantLock) PUT_LOCK_FIELD.get(this);
            Field notFullField = LinkedBlockingQueue.class.getDeclaredField("notFull");
            notFullField.setAccessible(true);
            
            putLock.lock();
            try {
                ((java.util.concurrent.locks.Condition) notFullField.get(this)).signalAll();
            } finally {
                putLock.unlock();
            }
        } catch (Exception e) {
            // 忽略唤醒失败，不影响主要功能
        }
    }

}
