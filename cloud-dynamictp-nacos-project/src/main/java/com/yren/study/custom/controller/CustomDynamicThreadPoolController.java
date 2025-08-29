package com.yren.study.custom.controller;

import com.yren.study.custom.manage.CustomDynamicThreadPoolManager;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author ChenYu ren
 * @date 2025/8/27
 */

@RestController
@RequestMapping("/custom/dynamic/threadPool/")
public class CustomDynamicThreadPoolController {


    @Resource
    private CustomDynamicThreadPoolManager threadPoolManager;

    /**
     * 获取线程池实时状态
     */
    @GetMapping("/status")
    public String getStatus() {
        return threadPoolManager.getThreadPoolStatus().toString();
    }

    /**
     * 动态调整线程池参数
     */
    @PostMapping("/adjust")
    public String adjust(@RequestParam int coreSize,
                         @RequestParam int maxSize,
                         @RequestParam int queueCapacity) {
        threadPoolManager.adjustThreadPool(coreSize, maxSize, queueCapacity);
        return "调整成功";
    }
}
