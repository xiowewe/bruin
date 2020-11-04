###  Dubbo 源码阅读笔记

> Dubbo文档：http://dubbo.apache.org/zh-cn/docs/user/quick-start.html
>
> 参考：https://segmentfault.com/a/1190000016741532

#### Dubbo项目模块

- dubbo-registry：注册中心模块

  基于注册中心下发地址的集群方式，以及对各种注册中心的抽象。包括Multicast、Zookeeper、Redis、Simple（默认）注册中心的实现。

- dubbo-cluster：集群模块

  多个invoker伪装成一个invoker，包括负载均衡、容错、路由等。集群地址列表可以是静态配置的，也可以是注册中心下发的。

- dubbo-config：dubbo对外提供API，隐藏dubbo所有细节。提供XML配置、属性配置、API配置、注解配置

- dubbo-rpc：远程调用模块

  抽象各种协议，以及动态代理，只包含一对一的调用，不关心集群的管理。依赖dubbo-remoting模块，抽象各类协议

- dubbo-remoting：远程通信模块

  dubbo协议的实现，如果rpc佣RMI协议不需要此包。

- dubbo-container：容器模块

  是一个Standlone的容器，以简单的Main加载Spring启动，通常不需要Tomcat/JBoss容器的特性

- dubbo-monitor：监控模块

  统计服务调用次数、调用时间、调用跟踪的服务

- dubbo-bootstrap-清理模块

- dubbo-common：通用模块

- dubbo-filter：过滤模块

- dubbo-serialization：徐磊话模块

  封装各种序列化框架的支持

#### Dubbo SPI拓展机制

SPI（Service Provider Interface)，是一种将服务接口与服务实现分离以达到解耦可拔插、大大提升了程序**可扩展性**的机制。

##### JDK、SpringBoot、Dubbo对比

- JDK SPI 机制

  `ServiceLoder`#`load`静态方法中实例化`ServiceLoder`，实例化过程创建`LazyIterator`，通过`LazyIterator`可以遍历"META-INF/services/"下配置的拓展资源。

  DriverManager静态代码块中`loadInitialDrivers`方法：

  ```java
  private static void loadInitialDrivers() {
      // ...
      // 如果驱动被打包作为服务提供者，则加载它。
      AccessController.doPrivileged(new PrivilegedAction<Void>() {
          public Void run() {
              // 1. load
              ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
              // 2. 获取Loader的迭代器
              Iterator<Driver> driversIterator = loadedDrivers.iterator();
              try{
                  // 3. 调用next方法
                  while(driversIterator.hasNext()) {
                      driversIterator.next();
                  }
              } catch(Throwable t) {
              // Do nothing
              }
              return null;
          }
      });
      // ...
  }
  ```

- SpringBoot SPI 机制

  SpringApplication 创建过程中，会涉及到ApplicatioContext和ApplicationListener的初始化的设置

  ```java
  setInitializers((Collection) getSpringFactoriesInstances(
        ApplicationContextInitializer.class));
  setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
  ```

  就是通过`SpringFactoriesLoader`#`loadSpringFactories`方法读取“META-INF/spring.factories”文件配置。SpringFactoriesLoader既可以单独加载配置然后按需实例化也可以实例化全部

- Dubbo SPI 机制

  Dubbo SPI 核心源码`ExtensionLoader`类中。`ExtensionLoader`会同时加载“META-INF/services”、“META-INF/dubbo”、“META-INF/dubbo/internal”路径下的配置，然后可以通过key、value获取，避免JDK类似的迭代遍历，减少性能损耗。并且增加了拓展点IOC和AOP的支持。

##### @SPI注解

在接口上加上@SPI注解后，表明该接口为可扩展接口。例如在Protocol上有@SPI("dubbo")注解，拓展dubbo、hessian、injvm、http等多种协议，默认的是dubbo协议。

##### @Adaptive注解

@Adaptive注解为了保证dubbo在调用具体实现的时候不是硬编码来指定引用哪个实现，适配一个接口的多种实现。

1. 在实现类上面加上@Adaptive注解，表明该实现类是该接口的适配器。例如：dubbo中的ExtensionFactory接口就有一个实现类AdaptiveExtensionFactory，加了@Adaptive注解，AdaptiveExtensionFactory就不提供具体业务支持，用来适配ExtensionFactory的SpiExtensionFactory和SpringExtensionFactory这两种实现。
2. 在接口方法上加@Adaptive注解，dubbo会动态生成适配器类。例如：Transport接口的bind、connect方法

##### @Activate注解

扩展点自动激活加载的注解，就是用条件来控制该扩展点实现是否被自动激活加载，在扩展实现类上面使用。它可以设置两个参数，分别是group和value。

##### ExtensionFactory接口

扩展工厂接口类，它本身也是一个扩展接口，有SPI的注解。该工厂接口提供的就是获取实现类的实例，它也有两种扩展实现，分别是`SpiExtensionFactory`和`SpringExtensionFactory`代表着两种不同方式去获取实例。而具体选择哪种方式去获取实现类的实例，则在适配器AdaptiveExtensionFactory中制定了规则。

##### ExtensionLoader源码



#### Dubbo Registry

dubbo内置的注册中心实现方式有四种（后期版本拓展了更多的注册中心），dubbo默认的注册中心实现方式，RegistryFactory接口的@SPI默认值是dubbo。

##### dubbo注册中心

##### multicast注册中心

##### redis注册中心

dubbo利用了redis的value支持map的数据类型。redis的key为服务名称和服务的类型。map中的key为URL地址，map中的value为过期时间，用于判断脏数据，脏数据由监控中心删除。dubbo利用JRedis来连接到Redis分布式哈希键-值数据库，因为Jedis实例不是线程安全的,所以不可以多个线程共用一个Jedis实例。同时使用JedisPool避免socket连接过多。

##### zookeeper注册中心

zookeeper数据结构

1. dubbo的Root层是根目录，通过<dubbo:registry group="dubbo" />的“group”来设置zookeeper的根节点，缺省值是“dubbo”。
2. Service层是服务接口的全名。
3. Type层是分类，一共有四种分类，分别是providers（服务提供者列表）、consumers（服务消费者列表）、routes（路由规则列表）、configurations（配置规则列表）。
4. URL层：根据不同的Type目录：可以有服务提供者 URL 、服务消费者 URL 、路由规则 URL 、配置规则 URL 。不同的Type关注的URL不同。

#### Dubbo Remoting

Dubbo通讯模块，通过SPI机制拓展了mina、netty、grizzly等各类NIO框架供用户自定义选择。

##### Buffer

##### Exchange

##### Telnet

##### Transport