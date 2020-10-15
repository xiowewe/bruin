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

官方文档https://github.com/Netflix/eureka/wiki/Understanding-eureka-client-server-communication#fetch-registry

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

Eureka客户端从服务器获取注册表信息，并将其本地缓存。之后，客户端使用该信息来查找其他服务。通过获取上一个获取周期与当前获取周期之间的增量更新，可以定期（每30秒）更新此信息。增量信息在服务器中保存的时间更长（大约3分钟），因此增量获取可能会再次返回相同的实例。 Eureka客户端会自动处理重复信息。获取增量后，Eureka客户端通过比较服务器返回的实例计数来与服务器协调信息，如果信息由于某种原因不匹配，则会再次获取整个注册表信息。 Eureka服务器缓存增量的压缩有效负载，整个注册表以及每个应用程序以及该应用程序的未压缩信息。有效负载还支持两种JSON / XML格式。 Eureka客户端使用jersey apache客户端以压缩的JSON格式获取信息。

1. Eureka客户端从服务器获取注册表信息，并将其本地缓存
2. 服务消费者就是从这些缓存信息中获取的服务提供者的信息
3. 增量更新的服务以30秒为周期循环调用
4. 增量更新数据在Server端保存时间为3分钟
5. Eureka client的增量更新，其实获取的是Eureka server最近三分钟内的变更，因此，如果Eureka client有超过三分钟没有做增量更新的话（例如网络问题），那么再调用增量更新接口时，那三分钟内Eureka server的变更就可能获取不到了，这就造成了Eureka server和Eureka client之间的数据不一致
6. 正常情况下，Eureka client多次增量更新后，最终的服务列表数据应该Eureka server保持一致，但如果期间发生异常，可能导致和Eureka server的数据不一致，为了暴露这个问题，Eureka server每次返回的增量更新数据中，会带有一致性哈希码，Eureka client用本地服务列表数据算出的一致性哈希码应该和Eureka server返回的一致，若不一致就证明增量更新出了问题导致Eureka client和Eureka server上的服务列表信息不一致了，此时需要全量更新
7. Eureka server上的服务列表信息对外提供JSON/XML两种格式下载
8. Eureka client使用jersey的SDK，去下载JSON格式的服务列表信息

源码分析从`CacheRefreshThread`#`refreshRegistry`方法开始，关键入口fetchRegistry方法

```java
private boolean fetchRegistry(boolean forceFullRegistryFetch) {
  //耗时处理
    Stopwatch tracer = FETCH_REGISTRY_TIMER.start();

    try {
        //获取本地缓存的服务列表信息
        Applications applications = getApplications();

      //判断多个条件，确定是否触发全量更新，如下任一个满足都会全量更新：
      //1. 是否禁用增量更新；
      //2. 是否对某个region特别关注；
      //3. 外部调用时是否通过入参指定全量更新；
      //4. 本地还未缓存有效的服务列表信息；
        if (clientConfig.shouldDisableDelta()
                || (!Strings.isNullOrEmpty(clientConfig.getRegistryRefreshSingleVipAddress()))
                || forceFullRegistryFetch
                || (applications == null)
                || (applications.getRegisteredApplications().size() == 0)
                || (applications.getVersion() == -1)) //Client application does not have latest library supporting delta
        {
          
            //。。。日志打印代码，可以看出触发全量更新的原因。。。
          
          //全量更行
            getAndStoreFullRegistry();
        } else {
          //增量更新
            getAndUpdateDelta(applications);
        }
      //重新计算和设置一致性hash码
        applications.setAppsHashCode(applications.getReconcileHashCode());
      //日志打印所有应用的所有实例数之和
        logTotalInstances();
    } catch (Throwable e) {
        logger.error(PREFIX + "{} - was unable to refresh its cache! status = {}", appPathIdentifier, e.getMessage(), e);
        return false;
    } finally {
        if (tracer != null) {
            tracer.stop();
        }
    }

    //将本地缓存更新的事件广播给所有已注册的监听器，注意该方法已被CloudEurekaClient类重写
    onCacheRefreshed();

    //检查刚刚更新的缓存中，有来自Eureka server的服务列表，其中包含了当前应用的状态，
    //当前实例的成员变量lastRemoteInstanceStatus，记录的是最后一次更新的当前应用状态，
    //上述两种状态在updateInstanceRemoteStatus方法中作比较 ，如果不一致，就更新		lastRemoteInstanceStatus，并且广播对应的事件
    updateInstanceRemoteStatus();

    // registry was fetched successfully, so return true
    return true;
}
```

###### 全量更新

`getAndStoreFullRegistry`方法负责全量更新。从eureka服务器获取完整的注册表信息，并将其存储在本地

```java
private void getAndStoreFullRegistry() throws Throwable {
    long currentUpdateGeneration = fetchRegistryGeneration.get();

    logger.info("Getting all instance registry info from the eureka server");

    Applications apps = null;
  //由于并没有配置特别关注的region信息，因此会调用eurekaTransport.queryClient.getApplications方法从服务端获取服务列表
    EurekaHttpResponse<Applications> httpResponse = clientConfig.getRegistryRefreshSingleVipAddress() == null
            ? eurekaTransport.queryClient.getApplications(remoteRegionsRef.get())
            : eurekaTransport.queryClient.getVip(clientConfig.getRegistryRefreshSingleVipAddress(), remoteRegionsRef.get());
    if (httpResponse.getStatusCode() == Status.OK.getStatusCode()) {
      //返回服务列表
        apps = httpResponse.getEntity();
    }
    logger.info("The response status is {}", httpResponse.getStatusCode());

    if (apps == null) {
        logger.error("The application is null for some reason. Not storing this information");
      //考虑到多线程同步，只有CAS成功的线程，才会把自己从Eureka server获取的数据来替换本地缓存
    } else if (fetchRegistryGeneration.compareAndSet(currentUpdateGeneration, currentUpdateGeneration + 1)) {
      //localRegionApps就是本地缓存，是个AtomicReference实例
        localRegionApps.set(this.filterAndShuffle(apps));
        logger.debug("Got full registry with apps hashcode {}", apps.getAppsHashCode());
    } else {
        logger.warn("Not updating applications as another thread is updating it already");
    }
}
```

getAndStoreFullRegistry方法中并无复杂逻辑，真正的服务列表获取逻辑在eurekaTransport.queryClient.getApplications(remoteRegionsRef.get())中，和Eureka server交互的逻辑都在这里面。具体实现在`AbstractJerseyEurekaHttpClient`#`getApplicationsInternal`方法中：利用jersey-client库的API向Eureka server发起restful请求，并将响应数据封装到EurekaHttpResponse实例中返回，并将响应的服务列表数据放在一个成员变量中作为本地缓存；

###### 增量更新

获取服务列表信息的增量更新是通过getAndUpdateDelta方法完成的

```java
private void getAndUpdateDelta(Applications applications) throws Throwable {
    long currentUpdateGeneration = fetchRegistryGeneration.get();

    Applications delta = null;
  //增量信息是通过eurekaTransport.queryClient.getDelta方法完成的
    EurekaHttpResponse<Applications> httpResponse = eurekaTransport.queryClient.getDelta(remoteRegionsRef.get());
    if (httpResponse.getStatusCode() == Status.OK.getStatusCode()) {
      //delta中保存了Eureka server返回的增量更新
        delta = httpResponse.getEntity();
    }

    if (delta == null) {
        logger.warn("The server does not allow the delta revision to be applied because it is not safe. "
                + "Hence got the full registry.");
      //如果增量信息为空，就直接发起一次全量更新
        getAndStoreFullRegistry();
      //考虑到多线程同步问题，这里通过CAS来确保请求发起到现在是线程安全的，
      //如果这期间fetchRegistryGeneration变了，就表示其他线程也做了类似操作，因此放弃本次响应的数据
    } else if (fetchRegistryGeneration.compareAndSet(currentUpdateGeneration, currentUpdateGeneration + 1)) {
        logger.debug("Got delta update with apps hashcode {}", delta.getAppsHashCode());
        String reconcileHashCode = "";
        if (fetchRegistryUpdateLock.tryLock()) {
            try {
              //用Eureka返回的增量数据和本地数据做合并操作
                updateDelta(delta);
              //用合并了增量数据之后的本地数据来生成一致性哈希码
                reconcileHashCode = getReconcileHashCode(applications);
            } finally {
                fetchRegistryUpdateLock.unlock();
            }
        } else {
            logger.warn("Cannot acquire update lock, aborting getAndUpdateDelta");
        }
       //Eureka server在返回增量更新数据时，也会返回服务端的一致性哈希码，
       //理论上每次本地缓存数据经历了多次增量更新后，计算出的一致性哈希码应该是和服务端一致的，
       //如果发现不一致，就证明本地缓存的服务列表信息和Eureka server不一致了，需要做一次全量更新
        if (!reconcileHashCode.equals(delta.getAppsHashCode()) || clientConfig.shouldLogDeltaDiff()) {
          //一致性哈希码不同，就在reconcileAndLogDifference方法中做全量更新
            reconcileAndLogDifference(delta, reconcileHashCode);  // this makes a remoteCall
        }
    } else {
        logger.warn("Not updating application delta as another thread is updating it already");
        logger.debug("Ignoring delta update with apps hashcode {}, as another thread is updating it already", delta.getAppsHashCode());
    }
}
```

关键点：

- a. 获取增量更新数据使用的方法是：eurekaTransport.queryClient.getDelta(remoteRegionsRef.get())；
  - 一般的增量更新是在请求中增加一个时间戳或者上次更新的tag号等参数，由服务端根据参数来判断哪些数据是客户端没有的；
  - 而这里的Eureka client却没有这类参数，联想到前面官方文档中提到的“Eureka会把更新数据保留三分钟”，就可以理解了：Eureka把最近的变更数据保留三分钟，这三分钟内每个Eureka client来请求增量更新是，server都返回同样的缓存数据，只要client能保证三分钟之内有一次请求，就能保证自己的数据和Eureka server端的保持一致；
  - 那么如果client有问题，导致超过三分钟才来获取增量更新数据，那就有可能client和server数据不一致了，此时就要有一种方式来判断是否不一致，如果不一致，client就会做一次全量更新，这种判断就是一致性哈希码；
- b. 将增量更新的数据和本地缓存合并的方法是： updateDelta(delta);
  -  检查每个服务的region，如果跨region的，就合并到另一个专门存放跨region服务的缓存中；
  - 增量数据中，对每个应用下实例的变动，分为新增、修改、删除三种，合并的过程就是对这三种数据在本地缓存中做不同的处理；
  - 合并过程中还会对缓存数据做整理，这样后续每次使用时，获取的多个实例其顺序是一样的；
- c. 通过检查一致性哈希码可以确定历经每一次增量更新后，本地的服务列表信息和Eureka server上的是否还保持一致，若不一致就要做一次全量更新，通过调用reconcileAndLogDifference方法来完成；

###### 更新缓存

onCacheRefreshed 方法，这是个spring容器内的广播，this.publisher的类型是ApplicationEventPublisher

###### 本地状态变化

updateInstanceRemoteStatus方法中，从Eureka server中取得的服务列表，自然也包括当前应用自己的信息，这个信息会保存在成员变量lastRemoteInstanceStatus中，每次更新了缓存后，都会用缓存中的信息和lastRemoteInstanceStatus对比，如果不一致，就表示在Eureka server端记录的当前应用状态发生了变化，此时就广播一次

##### 服务续约

官方文档https://github.com/Netflix/eureka/wiki/Understanding-eureka-client-server-communication#renew

1. Eureka client每隔三十秒发送一次心跳到Eureka server，这就是续约；
2. Eureka client续约的目的是告诉Eureka server自己还活着；
3. Eureka server若90秒内未收到心跳，就从自己的服务列表中剔除该Eureka client；
4. 建议不要改变心跳间隔，因为Eureka server是通过心跳来判断Eureka client是否正常；

