# 线程池使用痛点
线程池使用面临的核心的问题在于：**线程池的参数并不好配置。**一方面线程池的运行机制不是很好理解，配置合理需要强依赖开发人员的个人经验和知识；另一方面，线程池执行的情况和任务类型相关性较大，IO密集型和CPU密集型的任务运行起来的情况差异非常大，这导致业界并没有一些成熟的经验策略帮助开发人员参考。



# 依赖 API
> 如果看过 ThreadPoolExecutor 的源码，大概可以知道它对核心参数基本都有提供 set / get 方法 以及一些扩展方法，可以在运行时动态修改、获取相应的值。
>

```java
public void setCorePoolSize(int corePoolSize);
public void setMaximumPoolSize(int maximumPoolSize);
public void setKeepAliveTime(long time, TimeUnit unit);
public void setThreadFactory(ThreadFactory threadFactory);
public void setRejectedExecutionHandler(RejectedExecutionHandler handler);
public void allowCoreThreadTimeOut(boolean value);

public int getCorePoolSize();
public int getMaximumPoolSize();
public long getKeepAliveTime(TimeUnit unit);
public BlockingQueue<Runnable> getQueue();
public RejectedExecutionHandler getRejectedExecutionHandler();
public boolean allowsCoreThreadTimeOut();

protected void beforeExecute(Thread t, Runnable r);
protected void afterExecute(Runnable r, Throwable t);
```

# 最佳实践 & 官方文档
[Java线程池实现原理及其在美团业务中的实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html)

---

[接入步骤](https://dynamictp.cn/guide/use/quick-start.html)

---

<font style="color:rgb(51, 51, 51);"></font>

<font style="color:rgb(51, 51, 51);"></font>

# 线程池项目实践
## 版本
> `Jdk 8`
>
> `SpringBoot 2.7.18`
>
> `SpringCloud 2021.0.3`
>
> `Srping Cloud Alibaba 2021.0.1.0`
>
> `Dynamic-Tp-spring 1.2.2`
>
> `Dynamic-Tp-spring-boot-starter-nacos 1.2.2`
>
> `Apache-Maven-3.9.0`
>
> `Nacos 2.2.3`
>

## 依赖
> 父 POM
>

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.yren.study</groupId>
    <artifactId>yren-java-project-general-study</artifactId>
    <version>1.0</version>
    <packaging>pom</packaging>

    <modules>
        <module>cloud-dynamictp-nacos-project</module>
    </modules>

    <properties>
        <!-- JDK版本 -->
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <!-- Spring生态版本号管理 -->
        <spring.boot.version>2.7.18</spring.boot.version>
        <spring-cloud.version>2021.0.3</spring-cloud.version>
        <spring-cloud-alibaba.version>2021.0.1.0</spring-cloud-alibaba.version>
        <!--线程池监控-->
        <dynamic-tp-spring.version>1.2.2</dynamic-tp-spring.version>
        <dynamic-tp-spring-boot-starter-nacos.version>1.2.2</dynamic-tp-spring-boot-starter-nacos.version>
    </properties>

    <!-- 依赖版本管理 -->
    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring.boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Spring Cloud BOM -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Spring Cloud Alibaba BOM -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!--dynamic-tp -->
            <dependency>
                <groupId>org.dromara.dynamictp</groupId>
                <artifactId>dynamic-tp-spring</artifactId>
                <version>${dynamic-tp-spring.version}</version>
            </dependency>
            <!--dynamic-tp接入 nacos cloud -->
            <dependency>
                <groupId>org.dromara.dynamictp</groupId>
                <artifactId>dynamic-tp-spring-cloud-starter-nacos</artifactId>
                <version>${dynamic-tp-spring-boot-starter-nacos.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
</project>
```

> 主 POM
>

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.yren.study</groupId>
    <artifactId>yren-java-project-general-study</artifactId>
    <version>1.0</version>
  </parent>

  <artifactId>cloud-dynamictp-nacos-project</artifactId>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <!--用来暴露端点 配合监控组件 可以通过 http 方式实时获取指标数据-->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-bootstrap</artifactId>
    </dependency>
    <!--nacos服务发现依赖-->
    <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
      <scope>compile</scope>
    </dependency>

    <!--配置中心客户端-->
    <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    </dependency>

    <dependency>
      <groupId>org.dromara.dynamictp</groupId>
      <artifactId>dynamic-tp-spring</artifactId>
    </dependency>

    <dependency>
      <groupId>org.dromara.dynamictp</groupId>
      <artifactId>dynamic-tp-spring-cloud-starter-nacos</artifactId>
    </dependency>


  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <version>${spring.boot.version}</version>
      </plugin>
    </plugins>
  </build>

</project>
```

<font style="color:rgb(51, 51, 51);"></font>

## <font style="color:rgb(51, 51, 51);">配置文件</font>
> `bootstrap.yaml`
>

```yaml
server:
  port: 9892

spring:
  application:
    name: cloud-dynamictp-nacos-project
  profiles:
    active: dev

#actuator 配置
management:
  endpoints:
    web:
      exposure:
        include: "dynamictp"  # 暴露指定端点,或者使用 "*" 暴露所有端点（生产环境不推荐）
      path-mapping:
        # 将默认的 dynamictp 映射为 dynamic-tp,当前版本dynamictp,监控Endpoint为dynamictp,但是监控组件还是默认访问dynamic-tp所以需要转换
        dynamictp: dynamic-tp
  endpoint:
    dynamictp:
      enabled: true
```

<font style="color:rgb(51, 51, 51);"></font>

> `bootstrap-dev.yaml`
>

```yaml
--- #cloud基础配置
spring:
  cloud:
    nacos:
      # 组册服务配置
      discovery:
        # 给当前的服务命名 默认${spring.application.name}
        service: ${spring.application.name}
        server-addr: 127.0.0.1:8848
        # 命名空间
        namespace: 3084e72e-bfcd-40a3-8184-ecc4c7657b8e
        # 分组
        group: DEFAULT_GROUP
        # 账号信息
        username: nacos
        password: nacos
      # config 配置中心
      config:
        # 默认会加载 ${spring.application.name}-${spring.profiles.active}.type
        server-addr: ${spring.cloud.nacos.discovery.server-addr}
        namespace: ${spring.cloud.nacos.discovery.namespace}
        group: ${spring.cloud.nacos.discovery.group}
        file-extension: yaml
        # 账号信息
        username: ${spring.cloud.nacos.discovery.username}
        password: ${spring.cloud.nacos.discovery.password}
        extension-configs[0]:
          data-id: yren-dynamic-tp-nacos-demo.${spring.cloud.nacos.config.file-extension}
          # 刷新
          refresh: true
          group: ${spring.cloud.nacos.discovery.group}

```

<font style="color:rgb(51, 51, 51);"></font>

> **<font style="color:#DF2A3F;">该配置需上传 Nacos 配置中心 </font>**
>
> （Nacos 相关使用操作这里不做赘述）
>
> `yren-dynamic-tp-nacos-demo.yaml`
>

```yaml
access_token# 动态线程池配置
dynamictp:
  enabled: true
  enabledBanner: true           # 是否开启banner打印，默认true
  enabledCollect: true          # 是否开启监控指标采集，默认false
  collectorTypes: micrometer,logging,endpoint     # 监控数据采集器类型（logging | micrometer | internal_logging），默认micrometer
  logPath: /Users/yren/Desktop/logs           # 监控日志数据路径，默认 ${user.home}/logs
  monitorInterval: 5            # 监控时间间隔（报警判断、指标采集），默认5s
  platforms:                    # 通知报警平台配置
    # - platform: wechat
    #   urlKey: 3a7500-1287-4bd-a798-c5c3d8b69c  # 替换
    #   receivers: test1,test2                   # 接受人企微名称
    - platform: ding
      urlKey: ff1ffdbf1859f3bae84ee425fb3d69122b0c9027d935b1356bebba6f81ad159b  # 替换为DingWebHook access_token
      # secret: SECb5441fa6f375d5b9d21           # 替换，非sign模式可以没有此值
      receivers: 15968411164                   # 钉钉账号手机号
    # - platform: lark
    #   urlKey: 0d944ae7-b24a-40                 # 替换
    #   receivers: test1,test2                   # 接受人飞书名称/openid
  # tomcatTp:                                    # tomcat web server线程池配置
  #   corePoolSize: 100
  #   maximumPoolSize: 400
  #   keepAliveTime: 60
  # jettyTp:                                     # jetty web server线程池配置
  #   corePoolSize: 100
  #   maximumPoolSize: 400
  # undertowTp:                                  # undertow web server线程池配置
  #   corePoolSize: 100
  #   maximumPoolSize: 400
  #   keepAliveTime: 60
  # hystrixTp:                                   # hystrix 线程池配置
  #   - threadPoolName: hystrix1
  #     corePoolSize: 100
  #     maximumPoolSize: 400
  #     keepAliveTime: 60
  # dubboTp:                                     # dubbo 线程池配置
  #   - threadPoolName: dubboTp#20880
  #     corePoolSize: 100
  #     maximumPoolSize: 400
  #     keepAliveTime: 60
  # rocketMqTp:                                  # rocketmq 线程池配置
  #   - threadPoolName: group1#topic1
  #     corePoolSize: 200
  #     maximumPoolSize: 400
  #     keepAliveTime: 60
  executors:                                   # 动态线程池配置，都有默认值，采用默认值的可以不配置该项，减少配置量
    - threadPoolName: dtpExecutorExample1
      executorType: common                     # 线程池类型common、eager：适用于io密集型
      corePoolSize: 6
      maximumPoolSize: 99
      queueCapacity: 200
      queueType: VariableLinkedBlockingQueue   # 任务队列，查看源码QueueTypeEnum枚举类
      rejectedHandlerType: CallerRunsPolicy    # 拒绝策略，查看RejectedTypeEnum枚举类
      keepAliveTime: 50
      allowCoreThreadTimeOut: false                  # 是否允许核心线程池超时
      threadNamePrefix: test                         # 线程名前缀
      waitForTasksToCompleteOnShutdown: false        # 参考spring线程池设计，优雅关闭线程池
      awaitTerminationSeconds: 5                     # 单位（s）
      preStartAllCoreThreads: false                  # 是否预热所有核心线程，默认false
      runTimeout: 200                                # 任务执行超时阈值，目前只做告警用，单位（ms）
      queueTimeout: 100                              # 任务在队列等待超时阈值，目前只做告警用，单位（ms）
      taskWrapperNames: ["ttl"]                          # 任务包装器名称，集成TaskWrapper接口
      notifyItems:                     # 报警项，不配置自动会按默认值配置（变更通知、容量报警、活性报警、拒绝报警、任务超时报警）
        - type: capacity               # 报警项类型，查看源码 NotifyItemEnum枚举类
          enabled: true
          threshold: 80                # 报警阈值
          platforms: [ding,wechat]     # 可选配置，不配置默认拿上层platforms配置的所有平台
          interval: 120                # 报警间隔（单位：s）
        - type: change
          enabled: true
        - type: liveness
          enabled: true
          threshold: 80
        - type: reject
          enabled: true
          threshold: 1
        - type: run_timeout
          enabled: true
          threshold: 1
        - type: queue_timeout
          enabled: true
          threshold: 1
```

<font style="color:rgb(51, 51, 51);"></font>

## 代码
<font style="color:rgb(51, 51, 51);"></font>

### <font style="color:rgb(51, 51, 51);">启动类</font>
```java
/**
 * @author ChenYu ren
 * @date 2025/8/24
 */

@EnableDiscoveryClient
@EnableDynamicTp
@SpringBootApplication
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

<font style="color:rgb(51, 51, 51);"></font>

### 线程池代码声明
```java
/**
 * @author ChenYu ren
 * @date 2025/8/24
 */
@Configuration
public class ThreadPoolConfiguration {

    /**
     * 通过{@link DynamicTp} 注解定义普通juc线程池，会享受到该框架增强能力，注解名称优先级高于方法名
     * @return 线程池实例
     */
    @DynamicTp("jucThreadPoolExecutor")
    @Bean
    public ThreadPoolExecutor jucThreadPoolExecutor() {
        return new ThreadPoolExecutor(1, 999, 1, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    /**
     * 通过{@link DynamicTp} 注解定义spring线程池，会享受到该框架增强能力，注解名称优先级高于方法名
     * @return 线程池实例
     */
    @DynamicTp("threadPoolTaskExecutor")
    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    /**
     * 通过{@link ThreadPoolCreator} 快速创建一些简单配置的线程池，使用默认参数
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public DtpExecutor dtpExecutor0() {
        return ThreadPoolCreator.createDynamicFast("dtpExecutor0");
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建动态线程池
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public ThreadPoolExecutor dtpExecutor1() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("dtpExecutor1")
                .threadFactory("test-dtp-common")
                .corePoolSize(10)
                .maximumPoolSize(15)
                .keepAliveTime(40)
                .timeUnit(TimeUnit.SECONDS)
                .workQueue(MEMORY_SAFE_LINKED_BLOCKING_QUEUE.getName(), 2000)
                .buildDynamic();
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建动态线程池
     * eager，参考tomcat线程池设计，适用于处理io密集型任务场景，具体参数可以看代码注释
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public DtpExecutor eagerDtpExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("eagerDtpExecutor")
                .threadFactory("test-eager")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .queueCapacity(2000)
                .eager(true)
                .buildDynamic();
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建动态线程池
     * ordered，适用于处理有序任务场景，任务要实现Ordered接口，具体参数可以看代码注释
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public OrderedDtpExecutor orderedDtpExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("orderedDtpExecutor")
                .threadFactory("test-ordered")
                .corePoolSize(4)
                .maximumPoolSize(4)
                .queueCapacity(2000)
                .buildOrdered();
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建线程池
     * scheduled，适用于处理定时任务场景，具体参数可以看代码注释
     * @return 线程池实例
     */
    @Bean
    public ScheduledExecutorService scheduledDtpExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("scheduledDtpExecutor")
                .corePoolSize(2)
                .threadFactory("test-scheduled")
                .rejectedExecutionHandler(CALLER_RUNS_POLICY.getName())
                .buildScheduled();
    }

    /**
     * 通过{@link ThreadPoolBuilder} 设置详细参数创建线程池
     * priority，适用于处理优先级任务场景，具体参数可以看代码注释
     * tips: 建议直接在配置中心配置就行，不用@Bean声明
     * @return 线程池实例
     */
    @Bean
    public PriorityDtpExecutor priorityDtpExecutor() {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName("priorityDtpExecutor")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .threadFactory("test-priority")
                .rejectedExecutionHandler(CALLER_RUNS_POLICY.getName())
                .buildPriority();
    }
}
```

<font style="color:rgb(51, 51, 51);"></font>

### <font style="color:rgb(51, 51, 51);">线程池代码使用</font>
**从 **`**<font style="color:#DF2A3F;">DtpRegistry</font>**`** 中根据线程池名称获取，或者通过**`**<font style="color:#DF2A3F;">依赖注入</font>**`**方式（推荐，更优雅）**

---

> 依赖注入方式使用，优先推荐依赖注入方式，不能使用依赖注入的场景可以使用方式2
>

```java
@Resource
private ThreadPoolExecutor dtpExecutor1;

public void exec() {
   dtpExecutor1.execute(() -> System.out.println("test"));
}

```

---

> 从 DtpRegistry 注册器获取（接口场景可用）
>

```java
public static void main(String[] args) {
   DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor("dtpExecutor1");
   dtpExecutor.execute(() -> System.out.println("test"));
}
```



### HTTP 获取 DynamicTp 线程池状态
> ### 测试actuator endpoint & dynamictp 实时数据

```json
#GET http://127.0.0.1:9892/actuator/dynamic-tp

#Response

[{
	"poolName": "dtpExecutorExample1",
	"poolAliasName": null,
	"corePoolSize": 6,
	"maximumPoolSize": 99,
	"keepAliveTime": 50000,
	"queueType": "VariableLinkedBlockingQueue",
	"queueCapacity": 200,
	"queueSize": 0,
	"fair": false,
	"queueRemainingCapacity": 200,
	"activeCount": 0,
	"taskCount": 0,
	"completedTaskCount": 0,
	"largestPoolSize": 0,
	"poolSize": 0,
	"waitTaskCount": 0,
	"rejectCount": 0,
	"rejectHandlerName": "CallerRunsPolicy",
	"dynamic": true,
	"runTimeoutCount": 0,
	"queueTimeoutCount": 0,
	"tps": 0.0,
	"maxRt": 0,
	"minRt": 0,
	"avg": 0.0,
	"tp50": 0.0,
	"tp75": 0.0,
	"tp90": 0.0,
	"tp95": 0.0,
	"tp99": 0.0,
	"tp999": 0.0
}, {
	"poolName": "jucThreadPoolExecutor",
	"poolAliasName": null,
	"corePoolSize": 1,
	"maximumPoolSize": 999,
	"keepAliveTime": 1000,
	"queueType": "SynchronousQueue",
	"queueCapacity": 0,
	"queueSize": 0,
	"fair": false,
	"queueRemainingCapacity": 0,
	"activeCount": 0,
	"taskCount": 0,
	"completedTaskCount": 0,
	"largestPoolSize": 0,
	"poolSize": 0,
	"waitTaskCount": 0,
	"rejectCount": 0,
	"rejectHandlerName": "AbortPolicy",
	"dynamic": false,
	"runTimeoutCount": 0,
	"queueTimeoutCount": 0,
	"tps": 0.0,
	"maxRt": 0,
	"minRt": 0,
	"avg": 0.0,
	"tp50": 0.0,
	"tp75": 0.0,
	"tp90": 0.0,
	"tp95": 0.0,
	"tp99": 0.0,
	"tp999": 0.0
}, {
	"maxMemory": "3.56 GB",
	"totalMemory": "365 MB",
	"freeMemory": "323.93 MB",
	"usableMemory": "3.52 GB"
}]
```





# 动态线程池验证
> **当前线程池状态**
>
> `GET` [http://localhost:9892/actuator/dynamic-tp](http://localhost:9892/actuator/dynamic-tp)
>

```json
[{
		"poolName": "dtpExecutorExample1",
		"poolAliasName": null,
		"corePoolSize": 6,
		"maximumPoolSize": 99,
		"keepAliveTime": 50000,
		"queueType": "VariableLinkedBlockingQueue",
		"queueCapacity": 200,
		"queueSize": 0,
		"fair": false,
		"queueRemainingCapacity": 200,
		"activeCount": 0,
		"taskCount": 0,
		"completedTaskCount": 0,
		"largestPoolSize": 0,
		"poolSize": 0,
		"waitTaskCount": 0,
		"rejectCount": 0,
		"rejectHandlerName": "CallerRunsPolicy",
		"dynamic": true,
		"runTimeoutCount": 0,
		"queueTimeoutCount": 0,
		"tps": 0.0,
		"maxRt": 0,
		"minRt": 0,
		"avg": 0.0,
		"tp50": 0.0,
		"tp75": 0.0,
		"tp90": 0.0,
		"tp95": 0.0,
		"tp99": 0.0,
		"tp999": 0.0
	},
	{
		"maxMemory": "3.56 GB",
		"totalMemory": "370 MB",
		"freeMemory": "123.9 MB",
		"usableMemory": "3.32 GB"
	}
]
```

---

> **线程池配置动态修改 - 调整核心线程数量**
>
> 1. 调整核心线程数
>
>        dynamictp.executors[0].corePoolSize：6 -> 66
>
> 2. 发布配置
>

```yaml
# 动态线程池配置文件
dynamictp:
  executors:                                   # 动态线程池配置
    - threadPoolName: dtpExecutorExample1
      corePoolSize: 66 #调整核心线程数 [6 -> 66]
```

---

> **当前线程池状态**
>
> `GET` [http://localhost:9892/actuator/dynamic-tp](http://localhost:9892/actuator/dynamic-tp)
>
> **<font style="color:#8CCF17;">调整生效</font>**：corePoolSize = 66
>

```json
[{
	"poolName": "dtpExecutorExample1",
	"poolAliasName": null,
	"corePoolSize": 66,
	"maximumPoolSize": 99,
	"keepAliveTime": 50000,
	"queueType": "VariableLinkedBlockingQueue",
	"queueCapacity": 200,
	"queueSize": 0,
	"fair": false,
	"queueRemainingCapacity": 200,
	"activeCount": 0,
	"taskCount": 0,
	"completedTaskCount": 0,
	"largestPoolSize": 0,
	"poolSize": 0,
	"waitTaskCount": 0,
	"rejectCount": 0,
	"rejectHandlerName": "CallerRunsPolicy",
	"dynamic": true,
	"runTimeoutCount": 0,
	"queueTimeoutCount": 0,
	"tps": 0.0,
	"maxRt": 0,
	"minRt": 0,
	"avg": 0.0,
	"tp50": 0.0,
	"tp75": 0.0,
	"tp90": 0.0,
	"tp95": 0.0,
	"tp99": 0.0,
	"tp999": 0.0
}, {
	"maxMemory": "3.56 GB",
	"totalMemory": "935.5 MB",
	"freeMemory": "596.89 MB",
	"usableMemory": "3.22 GB"
}]
```

> **钉钉通知 - 调参通知**
>

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756274320973-34947663-8f9f-47bc-8f88-113a207944d8.png)



# 调参通知 & 运行报警
[调参通知](https://dynamictp.cn/guide/notice/notice.html)



## 通知报警相关配置
```yaml
# 动态线程池配置文件
dynamictp:
  enabled: true
  enabledBanner: true
  enabledCollect: true
  collectorTypes: micrometer,logging,endpoint
  logPath: /Users/yren/Desktop/logs
  monitorInterval: 5
  platforms: #通知报警平台
    - platform: ding
      urlKey: ff1ffdbf1859f3bae84ee425fb3d69122b0c9027d935b1356bebba6f81ad159b #WebHoook-access_token
      # secret: SECb5441fa6f375d5b9d21  #机器人安全设置-签名
      receivers: 15968411164 # 通知人钉钉账号手机号
  executors: # 动态线程池配置
    - threadPoolName: dtpExecutorExample1
      notifyItems: # 报警项
        - type: capacity        # 报警项类型，查看源码 NotifyItemEnum枚举类
          enabled: true
          threshold: 80         # 报警阈值
          platforms: [ding]     # 可选配置，不配置默认拿上层platforms所有平台
          interval: 120         # 报警间隔（单位：s）
        - type: change
          enabled: true
        - type: liveness
          enabled: true
          threshold: 80
        - type: reject
          enabled: true
          threshold: 1
        - type: run_timeout
          enabled: true
          threshold: 1
        - type: queue_timeout
          enabled: true
          threshold: 1
```



## 通知效果
> 例如以下调参通知
>

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756275135563-76b51f55-4047-4d8c-84be-8948b94f8beb.png)











# HertzBeat 实时监控
****

> HertzBeat 是一款开源，易用友好的实时监控系统，无需Agent，拥有强大自定义监控能力。
>
> 支持对应用服务，数据库，操作系统，中间件，云原生等监控，阈值告警，**<font style="color:#E4495B;">告警通知</font>**（邮件微信钉钉飞书短信 Slack Discord Telegram）。<font style="color:rgba(235, 235, 245, 0.86);background-color:rgb(27, 27, 31);"></font>
>



## 官方
[Apache HertzBeat · An open source, real-time monitoring tool with custom-monitor and agentLess. | Apache HertzBeat](https://hertzbeat.apache.org/)

## 安装（Docker）
```shell
docker run -d -p 1157:1157 -p 1158:1158 \
    -v $(pwd)/data:/opt/hertzbeat/data \
    -v $(pwd)/logs:/opt/hertzbeat/logs \
    -v $(pwd)/application.yml:/opt/hertzbeat/config/application.yml \
    -v $(pwd)/sureness.yml:/opt/hertzbeat/config/sureness.yml \
    --restart=always \
    --name hertzbeat apache/hertzbeat
```

### 命令参数详解
+ `docker run -d` ：通过 Docker 后台运行容器
+ `-p 1157:1157 -p 1158:1158` : 映射容器端口到主机端口(前面是宿主机的端口号，后面是容器的端口号)。1157是页面端口，1158是集群端口。
+ `-v $(pwd)/data:/opt/hertzbeat/data` : (可选，数据持久化) 重要，挂载数据库文件到本地主机，保证数据不会因为容器的创建删除而丢失
+ `-v $(pwd)/logs:/opt/hertzbeat/logs` : (可选) 挂载日志文件到本地主机方便查看
+ `-v $(pwd)/application.yml:/opt/hertzbeat/config/application.yml` : (可选) 挂载配置文件到容器中(请确保本地已有此文件)。[下载源](https://github.com/apache/hertzbeat/raw/master/script/application.yml)
+ `-v $(pwd)/sureness.yml:/opt/hertzbeat/config/sureness.yml` : (可选) 挂载账户配置文件到容器中(请确保本地已有此文件)。[下载源](https://github.com/apache/hertzbeat/raw/master/script/sureness.yml)
+ `-v $(pwd)/ext-lib:/opt/hertzbeat/ext-lib` : (可选) 挂载外部的第三方 JAR 包 [mysql-jdbc](https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-8.0.25.zip) [oracle-jdbc](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/23.4.0.24.05/ojdbc8-23.4.0.24.05.jar) [oracle-i18n](https://repo.mavenlibs.com/maven/com/oracle/database/nls/orai18n/21.5.0.0/orai18n-21.5.0.0.jar)
+ `--name hertzbeat` : (可选) 命名容器名称为 hertzbeat
+ `--restart=always` : (可选) 配置容器自动重启。
+ `apache/hertzbeat` : 使用[官方应用镜像](https://hub.docker.com/r/apache/hertzbeat)来启动容器, 若网络超时可用`quay.io/tancloud/hertzbeat`代替。



### 验证访问
> **默认账户：**admin **密码：**hertzbeat
>
> 右上角可以切换中文
>

```shell
#GET http://ip:1157
```

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756227117429-06cd9733-1f6d-4b08-9a66-6c20628d24fe.png)

## 监控能力
> **新增监控 -> 应用程序监控 -> DynamicTp 线程池 -> 填写配置**
>

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756279846103-0e2a161f-be12-4fe2-9630-7c577928c0ee.png)

---

> **配置项说明**
>

| **<font style="color:rgb(28, 30, 33);">参数名称</font>** | **<font style="color:rgb(28, 30, 33);">参数帮助描述</font>** |
| --- | --- |
| <font style="color:rgb(28, 30, 33);">监控Host</font> | <font style="color:rgb(28, 30, 33);">被监控的对端IPV4，IPV6或域名。注意</font><font style="color:rgb(28, 30, 33);">⚠️</font><font style="color:rgb(28, 30, 33);">不带协议头(eg: https://, http://)。</font> |
| <font style="color:rgb(28, 30, 33);">任务名称</font> | <font style="color:rgb(28, 30, 33);">标识此监控的名称，名称需要保证唯一性。</font> |
| <font style="color:rgb(28, 30, 33);">端口</font> | <font style="color:rgb(28, 30, 33);">应用服务对外提供的端口，默认为8080。</font> |
| <font style="color:rgb(28, 30, 33);">启用HTTPS</font> | <font style="color:rgb(28, 30, 33);">是否通过HTTPS访问网站，注意</font><font style="color:rgb(28, 30, 33);">⚠️</font><font style="color:rgb(28, 30, 33);">开启HTTPS一般默认对应端口需要改为443</font> |
| <font style="color:rgb(28, 30, 33);">Base Path</font> | <font style="color:rgb(28, 30, 33);">暴露接口路径前缀，默认 /actuator</font> |
| <font style="color:rgb(28, 30, 33);">采集间隔</font> | <font style="color:rgb(28, 30, 33);">监控周期性采集数据间隔时间，单位秒，可设置的最小间隔为30秒</font> |
| <font style="color:rgb(28, 30, 33);">是否探测</font> | <font style="color:rgb(28, 30, 33);">新增监控前是否先探测检查监控可用性，探测成功才会继续新增修改操作</font> |
| <font style="color:rgb(28, 30, 33);">描述备注</font> | <font style="color:rgb(28, 30, 33);">更多标识和描述此监控的备注信息，用户可以在这里备注信息</font> |


![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756280276763-fd9990d5-3cfd-464a-b23d-67d8e4c92e05.png)

> **查看线程池监控**
>

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756280333773-b00afd56-f443-4b3e-acdb-c521ee423fb7.png)

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756280414466-7037f84f-925b-4086-8bd9-ee5af606ed3c.png)

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756280433435-cb4b96d5-7c30-4a70-a9f1-7f21d9e138c3.png)





## 告警能力
### 阈值规则配置
> 系统将根据配置喝采集指标数据计算出发告警
>

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756280770799-2b2f464b-91fa-4797-afa5-3f9f31a152c8.png)

> **关联监控**
>

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756280831712-f4321d62-78b6-47bb-9f4c-98f817c36b80.png)

> **新增阈值规则**
>

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756281159776-41f2dd1d-9667-4374-ab97-3656d7282a2b.png)![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756281279221-df67ef45-f9d0-41ec-9c32-42252a23d323.png)





### 消息通知配置
> <font style="color:rgba(0, 0, 0, 0.85);">消息通知用于配置告警通知的接收对象以及接收方式，使被阈值触发的告警信息，通过指定方式通知到接收对象（支持邮箱、钉钉、微信，Webhook等）</font>
>

#### 新增接收对象
![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756281602243-32476fc9-63fa-4075-ad9d-ba6040b2982b.png)



#### 通知策略
![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756281703800-d7f06cd5-b238-4db9-9c27-372a9067ac4d.png)







## 代码测试告警通知
> 上文配置了活跃线程池线程数量 **>2** 就会告警
>
> GET [http://localhost:9892/test](about:blank) 一分钟内发送三次请求创建 3 个线程处理任务
>

```java
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
```

> DingDing 机器人通知如下
>

![](https://cdn.nlark.com/yuque/0/2025/png/29168630/1756282026560-165cdf5f-e0fd-494d-9046-04e35dbd3c15.png)

