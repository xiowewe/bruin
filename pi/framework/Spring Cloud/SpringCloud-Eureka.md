#### Eureka Server

##### Server初始化过程

- **@EnableEurekaServer** 入口

- **EurekaServerMarkerConfiguration** 

  EnableEurekaServer引入的配置类，用来激活EurekaServerAutoConfiguration

- **EurekaServerAutoConfiguration** 

  ```java
  @Configuration //配置类
  @Import(EurekaServerInitializerConfiguration.class) //引入配置
  //存在Marker时就会注入该类
  @ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class) 
  @EnableConfigurationProperties({ EurekaDashboardProperties.class,
        InstanceRegistryProperties.class })
  @PropertySource("classpath:/eureka/server.properties")
  ```

  - 实例化EurekaServerContext、EurekaServerBootstrap
  - 通过内部类EurekaServerConfigBeanConfiguration 设置配置信息，实例EurekaServerConfig类型Bean
  - EurekaServerInitializerConfiguration 初始化操作

- **EurekaServerInitializerConfiguration**

  由于实现了Lifecycle接口，因此会被spring容器回调start方法

  ```java
  public void start() {
     new Thread(new Runnable() {
        @Override
        public void run() {
           try {
  //初始化eureka server的context、environment       
      eurekaServerBootstrap.contextInitialized(EurekaServerInitializerConfiguration.this.servletContext);
              log.info("Started Eureka Server");
  						//发送广播，将EurekaServer的配置信息广播给全部订阅了该类型消息的监听
              publish(new EurekaRegistryAvailableEvent(getEurekaServerConfig()));
              EurekaServerInitializerConfiguration.this.running = true;
              publish(new EurekaServerStartedEvent(getEurekaServerConfig()));
           }
           catch (Exception ex) {
              // Help!
              log.error("Could not initialize Eureka servlet context", ex);
           }
        }
     }).start();
  }
  ```



#### Eureka Client

##### EnableDiscoveryClient 和 EnableEurekaClient区别

在区分两者之前，先了解下Spring cloud 版本：Angle -> Brixton -> Camden -> Dalston -> Edgware -> Finchley

在Dalston或更早的版本时，EnableDiscoveryClient 和 EnableEurekaClient 都能实现client的服务注册，EnableEurekaClient注解使用了EnableDiscoveryClient的注解（之后的版本则没有）。在官方的解答https://stackoverflow.com/questions/31976236/whats-the-difference-between-enableeurekaclient-and-enablediscoveryclient中解释：注册发现服务有三种实现方式：eureka、consul、zookeeper，EnableDiscoveryClient注解在common包中，通过项目的classpath来决定使用哪种实现，而EnableEurekaClient注解在netflix包中，只会使用eureka这种实现方式（并不是很明白）

在Dalston版本之后，“@EnableDiscoveryClient is now optional” 用或不用都不影响服务注册发现功能，且EnableEurekaClient 基本上不使用。为什么说@EnableDiscoveryClient 可以用可不用，实际上spring-cloud-netflix-eureka-client包中的spring.factories文件已经自动帮我们实例化EnableDiscoveryClient Bean了

**spring.factories**

```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.cloud.netflix.eureka.config.EurekaClientConfigServerAutoConfiguration,\
org.springframework.cloud.netflix.eureka.config.EurekaDiscoveryClientConfigServiceAutoConfiguration,\
org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration,\
org.springframework.cloud.netflix.ribbon.eureka.RibbonEurekaAutoConfiguration,\
org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration

org.springframework.cloud.bootstrap.BootstrapConfiguration=\
org.springframework.cloud.netflix.eureka.config.EurekaDiscoveryClientConfigServiceBootstrapConfiguration
```

**EurekaDiscoveryClientConfiguration**

```java
@Bean
public DiscoveryClient discoveryClient(EurekaInstanceConfig config, EurekaClient client) {
   return new EurekaDiscoveryClient(config, client);
}
```

##### 服务注册

1. @EnableDiscoveryClient 作为入口

   import导入`EnableDiscoveryClientImportSelector`配置，其继承并重写了`SpringFactoryImportSelector`的`selectImport`方法，获取父类构建的注解元信息，父类会通过SPI加载spring.factories配置

2. spring.factories 配置了`EurekaDiscoveryClientConfiguration`、`EurekaClientConfigServerAutoConfiguration`等5个配置类

3. EurekaDiscoveryClientConfiguration

   ```java
   @Bean
   public DiscoveryClient discoveryClient(EurekaInstanceConfig config, EurekaClient client) {
     	//client 参数为EurekaClient类型，由EurekaDiscoveryClientConfiguration内部类的eurekaClient 进行懒加载
      return new EurekaDiscoveryClient(config, client);
   }
   ```

   ```java
   @Bean(destroyMethod = "shutdown")
   @ConditionalOnMissingBean(value = EurekaClient.class, search = SearchStrategy.CURRENT)
   @org.springframework.cloud.context.config.annotation.RefreshScope
   @Lazy
   public EurekaClient eurekaClient(ApplicationInfoManager manager, EurekaClientConfig config, EurekaInstanceConfig instance) {
      manager.getInfo(); // force initialization
     //DiscoveryClient 的子类
      return new CloudEurekaClient(manager, config, this.optionalArgs,
            this.context);
   }
   ```

   CloudEurekaClient的父类com.netflix.discovery.DiscoveryClient来自netflix发布的eureka-client包中，所以可以这么理解：EurekaDiscoveryClient类是个代理身份，真正的服务注册发现是委托给netflix的开源包来完成的，我们直接看DiscoveryClient的构造方法

4. DiscoveryClient

   DiscoveryClient的构造方法多且复杂，我们在其中能看到常见的一些log，例如:

   ```java
   logger.info("Initializing Eureka in region {}", clientConfig.getRegion());
   ```

   **最关键的方法initScheduledTasks**

   ```java
   // finally, init the schedule tasks (e.g. cluster resolvers, heartbeat, instanceInfo replicator, fetch
   initScheduledTasks();
   ```

5. **initScheduledTasks**方法

   ```java
   /**
    * Initializes all scheduled tasks.
    */
   private void initScheduledTasks() {
     //获取服务注册列表
       if (clientConfig.shouldFetchRegistry()) {
           // 服务注册列表更新的周期时间
           int registryFetchIntervalSeconds = clientConfig.getRegistryFetchIntervalSeconds();
           int expBackOffBound = clientConfig.getCacheRefreshExecutorExponentialBackOffBound();
         //定时更新服务注册列表
           scheduler.schedule(
                   new TimedSupervisorTask(
                           "cacheRefresh",
                           scheduler,
                           cacheRefreshExecutor,
                           registryFetchIntervalSeconds,
                           TimeUnit.SECONDS,
                           expBackOffBound,
                           new CacheRefreshThread()//该线程执行更新的具体逻辑
                   ),
                   registryFetchIntervalSeconds, TimeUnit.SECONDS);
       }
   
       if (clientConfig.shouldRegisterWithEureka()) {
         //服务续约的周期时间
           int renewalIntervalInSecs = instanceInfo.getLeaseInfo().getRenewalIntervalInSecs();
           int expBackOffBound = clientConfig.getHeartbeatExecutorExponentialBackOffBound();
           logger.info("Starting heartbeat executor: " + "renew interval is: {}", renewalIntervalInSecs);
   
           // 定时心跳续约
           scheduler.schedule(
                   new TimedSupervisorTask(
                           "heartbeat",
                           scheduler,
                           heartbeatExecutor,
                           renewalIntervalInSecs,
                           TimeUnit.SECONDS,
                           expBackOffBound,
                           new HeartbeatThread()//该线程执行续约的具体逻辑
                   ),
                   renewalIntervalInSecs, TimeUnit.SECONDS);
   
          //上报自身信息到Eureka server的操作委托给InstanceInfoReplicator实例发起，
          //如果有多个场景需要上报，都由InstanceInfoReplicator进行调度和安排，
          //并且还有限流逻辑，避免频繁先服务端请求
           instanceInfoReplicator = new InstanceInfoReplicator(
                   this,
                   instanceInfo,
                   clientConfig.getInstanceInfoReplicationIntervalSeconds(),
                   2); // burstSize
   
         //监听和响应应用状态变化，包括从停止状态恢复或者进入停止状态，
           statusChangeListener = new ApplicationInfoManager.StatusChangeListener() {
               @Override
               public String getId() {
                   return "statusChangeListener";
               }
   
               @Override
               public void notify(StatusChangeEvent statusChangeEvent) {
                   if (InstanceStatus.DOWN == statusChangeEvent.getStatus() ||
                           InstanceStatus.DOWN == statusChangeEvent.getPreviousStatus()) {
                       // log at warn level if DOWN was involved
                       logger.warn("Saw local status change event {}", statusChangeEvent);
                   } else {
                       logger.info("Saw local status change event {}", statusChangeEvent);
                   }
                 //将自身状态上报都Eureka server（有限流逻辑避免频繁上报）
                   instanceInfoReplicator.onDemandUpdate();
               }
           };
   
           if (clientConfig.shouldOnDemandUpdateStatusChange()) {
               applicationInfoManager.registerStatusChangeListener(statusChangeListener);
           }
   //更新信息并注册到Eureka server
           instanceInfoReplicator.start(clientConfig.getInitialInstanceInfoReplicationIntervalSeconds());
       } else {
           logger.info("Not registering with Eureka server per configuration");
       }
   }
   ```

6. InstanceInfoReplicator

   InstanceInfoReplicator是个辅助类，在服务注册过程中主要负责并发控制、周期性执行等工作。注册主要关注run方法

   ```java
   public void run() {
       try {
         //刷新当前的本地instanceInfo。请注意，在观察到更改的有效刷新之后，instanceInfo上的 isDirty标志设置为true
           discoveryClient.refreshInstanceInfo();
           Long dirtyTimestamp = instanceInfo.isDirtyWithTime();
           if (dirtyTimestamp != null) {
             //注册请求类型是Restful的，Eureka server的返回码如果是204表示注册成功
               discoveryClient.register();
               instanceInfo.unsetIsDirty(dirtyTimestamp);
           }
       } catch (Throwable t) {
           logger.warn("There was a problem with the instance info replicator", t);
       } finally {
           Future next = scheduler.schedule(this, replicationIntervalSeconds, TimeUnit.SECONDS);
           scheduledPeriodicRef.set(next);
       }
   }
   ```

   DiscoveryClient类的register方法

   ```java
   /**
    * Register with the eureka service by making the appropriate REST call.
    */
   boolean register() throws Throwable {
       logger.info(PREFIX + "{}: registering service...", appPathIdentifier);
       EurekaHttpResponse<Void> httpResponse;
       try {
         //JerseyApplicationClient 来完成注册操作,源码在其父类AbstractJerseyEurekaHttpClient中
           httpResponse = eurekaTransport.registrationClient.register(instanceInfo);
       } catch (Exception e) {
           logger.warn(PREFIX + "{} - registration failed {}", appPathIdentifier, e.getMessage(), e);
           throw e;
       }
       if (logger.isInfoEnabled()) {
           logger.info(PREFIX + "{} - registration status: {}", appPathIdentifier, httpResponse.getStatusCode());
       }
       return httpResponse.getStatusCode() == 204;
   }
   ```



##### 更新服务列表

##### 服务续约

