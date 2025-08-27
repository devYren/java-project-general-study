package com.yren.study.controller;

import org.checkerframework.checker.units.qual.C;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * @author ChenYu ren
 * @date 2025/8/24
 */

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource(name = "jucThreadPoolExecutor")
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping
    public String test() {
        threadPoolExecutor.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(60);
                System.out.println("执行完成");
            } catch (Exception ignored) {
            }
        });
        return "success";
    }

}
