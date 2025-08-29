package com.yren.study.custom.handel;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.yren.study.custom.manage.CustomDynamicThreadPoolManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;

import java.util.Map;
import java.util.concurrent.Executor;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;

/**
 * Nacos配置刷新处理器
 *
 * @author ChenYu ren
 * @date 2025/8/28
 */
@Slf4j
@Component
public class NacosCloudRefresherHandler implements ApplicationRunner {

    @Resource
    private NacosConfigManager nacosConfigManager;

    @Resource
    private NacosConfigProperties nacosConfigProperties;

    @Resource
    private CustomDynamicThreadPoolManager customDynamicThreadPoolManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ConfigService configService = nacosConfigManager.getConfigService();
        log.info("NacosConfigManager获取成功: {}", configService);
        log.info("Nacos配置属性: serverAddr={}, namespace={}, group={}",
                nacosConfigProperties.getServerAddr(),
                nacosConfigProperties.getNamespace(),
                nacosConfigProperties.getGroup());
        configService.addListener("custom-dynamic-tp-nacos-demo.yaml", DEFAULT_GROUP, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }
            @Override
            public void receiveConfigInfo(String newConfig) {

                // 解析新配置并更新线程池
                try {
                    // 使用YAML解析器
                    Yaml yaml = new Yaml();
                    Map<String, Object> config = yaml.load(newConfig);

                    // 解析嵌套的配置结构
                    Map<String, Object> customDynamic = (Map<String, Object>) config.get("custom-dynamic");
                    if (customDynamic != null) {
                        Map<String, Object> threadpool = (Map<String, Object>) customDynamic.get("threadpool");
                        if (threadpool != null) {
                            Integer newCoreSize = (Integer) threadpool.get("core-size");
                            Integer newMaxSize = (Integer) threadpool.get("max-size");
                            Integer newQueueCapacity = (Integer) threadpool.get("queue-capacity");

                            if (newCoreSize != null && newMaxSize != null && newQueueCapacity != null) {
                                customDynamicThreadPoolManager.adjustThreadPool(newCoreSize, newMaxSize, newQueueCapacity);
                                log.info("线程池配置已更新 - coreSize: {}, maxSize: {}, queueCapacity: {}",
                                        newCoreSize, newMaxSize, newQueueCapacity);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("解析YAML配置失败", e);
                }
            }
        });
    }

}
