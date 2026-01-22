## 📚简介

封装公司公用的基础组件

## 📦安装

### 🍊Maven

在项目的pom.xml的dependencies中加入以下内容:

```xml
<dependency>
    <groupId>com.supercode.framework</groupId>
    <artifactId>supercode-framework</artifactId>
    <version>x.x.x</version>
</dependency>
```

### 🎋版本说明

| 版本号   | 功能                          |
|-------|-----------------------------|
| 2.0.0 | 封装httpClient的工具包            |
| 2.0.1 | 封装线程池抽象类                    |
| 2.0.2 | jackson升级                   |
| 2.1.0 | 线程池引入cat跨线程传递、包名fix         |
| 2.1.1 | 线程池添加cat监控、添加@Async默认配置     |
| 2.1.2 | cat-client升级                |
| 2.1.3 | 封装BeanDefinition，提供注入IOC的能力 |
| 2.1.5 | jackson序列化：精度、时间戳           |
| 2.2.0 | 日志相关                        |
| 2.3.0 | 升级parent                    |
| 2.3.1 | JSON反序列化（Map）               |
| 2.3.2 | RestClient：支持SSL            |
| 2.3.3 | RestClient：支持PUT            |

------

### 🧬如何使用RestClient

#### step1 : 初始化

    public static final RestClient LOW = new RestClient(300, 300, 15, 50);
    public static final RestClient DEFAULT = new RestClient(1000, 1000, 15, 50);
    public static final RestClient HIGH = new RestClient(2000, 2000, 15, 50);
    public static final RestClient DANGER_HIGH = new RestClient(18000, 18000);

#### step2 : 使用

     (1) 将response转换为String
     String result = RestClient.DEFAULT
                               .postJsonAuth(CREATE_ACCOUNT_API, userName, password, params, String.class, null, null);

     (2) 将response转换为特定对象    
     List<String> list = RestClient.DEFAULT
                .post( CREATE_ACCOUNT_API, new TypeReference<List<String>>() {}, params);

     (3) 保留最原始的对象    
     HttpResponse httpResponse = RestClient.DEFAULT
                   .getAuth(CREATE_ACCOUNT_API, userName, password, HttpResponse.class, null, null);

----

### 🧬如何使用AbstractThreadPoolBasicExecutor

#### step1 : 实例化

    (1) 指定线程池名称，默认拒绝策略使用AbortPolicy
    public final class ExampleExecutor extends AbstractThreadPoolBasicExecutor {
        private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ExampleExecutor();
        private ExampleExecutor() {
            super("example-overview");
        }
        public static ThreadPoolExecutor getInstance() {
            return THREAD_POOL_EXECUTOR;
        }
    }

    (2) 指定线程池名称+线程池拒绝策略
    public final class ExampleExecutor extends AbstractThreadPoolBasicExecutor {
        private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ExampleExecutor();
        private ExampleExecutor() {
            super("example-overview", getCallerRunsPolicy());
            // super("example-overview", getAbortPolicy());
            // super("example-overview", getDiscardPolicy());
            // super("example-overview", getDiscardOldestPolicy());
        }
        public static ThreadPoolExecutor getInstance() {
            return THREAD_POOL_EXECUTOR;
        }
    }

    (3) 指定线程池全量入参
    public final class ExampleExecutor extends AbstractThreadPoolBasicExecutor {
        private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ExampleExecutor();
        private ExampleExecutor() {
            super(
                4,
                8,
                5,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactoryBuilder().setNameFormat("example-executor-%d").build(),
                getCallerRunsPolicy()
            );
        }
        public static ThreadPoolExecutor getInstance() {
            return THREAD_POOL_EXECUTOR;
        }
    }

#### step2 : 使用

    ThreadPoolExecutor executor = ExampleExecutor.getInstance();
    executor.execute(() -> doSomething());
    executor.submit(() -> doSomething());

----