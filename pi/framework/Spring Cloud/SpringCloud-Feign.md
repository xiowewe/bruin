### Feign

Feign远程调用，核心就是通过一系列的封装和处理，将以JAVA注解的方式定义的远程调用API接口，最终转换成HTTP的请求形式，然后将HTTP的请求的响应结果，解码成JAVA Bean，放回给调用者。

#### EnableFeignClient 原理

`@EnableFeignClients` 注解，相当于启用了`feign`客户端定义的扫描和注册机制，从而可以发现注解`@FeignClient`定义的`feign`客户端，并最终作为`bean`定义注册到容器中。而通过`@Autowired`自动装配注解，这些`feign`客户端会以`ReflectiveFeign$FeignInvocationHandler`动态代理的形式被注入到使用方。该`feign`客户端包含了对每个接口方法的处理器`MethodHandler`,接口缺省方法对应`DefaultMethodHandler`,服务功能端点方法对应`SynchronousMethodHandler`。

##### FeignClientsRegistrar

`FeignClientsRegistrar`实现了接口 `ImportBeanDefinitionRegistrar`，配合使用`@Configuration`注解的使用者配置类，在配置类被处理时，用于额外注册一部分`bean`。

源码详情可参考：https://blog.csdn.net/andy_zhang2007/article/details/86680622

- registerBeanDefinitions

  ```java
  @Override
     public void registerBeanDefinitions(AnnotationMetadata metadata,BeanDefinitionRegistry registry) {
     	// 注册缺省配置到容器 registry
     	registerDefaultConfiguration(metadata, registry);
     	// 注册所发现的各个 feign 客户端到到容器 registry
     	registerFeignClients(metadata, registry);
     }
  ```

- registerDefaultConfiguration

  *注册feign客户端的缺省配置，缺省配置信息来自注解元数据的属性 defaultConfiguration* 

- registerFeignClients

  对于每个`@FeignClient`注解的`feign`客户端定义 :

  1. 扫描basePackage包，针对每个`feign`注册一个客户端的配置`BeanDefinition`定义；
  2. 注册该`feign`客户端`bean`定义,指定生成`bean`实例采用工厂类`FeignClientFactoryBean`;

#### FeignClientFactoryBean获取

日常开发中Autowired Feign客户端，通过FeignClientFactoryBean#getObject 获取Feign 客户端Proxy实例

```java
@Override
public Object getObject() throws Exception {
  return getTarget();
}

<T> T getTarget() {
  //  从应用上下文中获取创建 feign 客户端的上下文对象 FeignContext
  // FeignContext 针对每个feign客户端定义会生成一个不同的 AnnotationConfigApplicationContext，
  // 这些应用上下文的parent都设置为当前应用的主应用上下文
  FeignContext context = applicationContext.getBean(FeignContext.class);
  // 为目标feign客户端对象构建一个 builder,该builder最终生成的目标feign客户端是一个
  // 动态代理，使用 InvocationHandler ： ReflectiveFeign$FeignInvocationHandler
  Feign.Builder builder = feign(context);

  if (!StringUtils.hasText(this.url)) {
    // @FeignClient 属性 url 属性没有指定的情况         
    // 根据属性 name , path 拼装一个 url，
    // 这种通常是需要在多个服务节点之间进行负载均衡的情况
    if (!this.name.startsWith("http")) {
      url = "http://" + this.name;
    }
    else {
      url = this.name;
    }
    // 方法cleanPath()加工属性path，使其以/开头，不以/结尾
    url += cleanPath();
    // 这里形成的url格式类似 :  http://test-service/test
    // 其中 test-service 是服务名，不是服务所在节点的IP，主机名或者域名

    // 函数 loadBalance 做如下动作 :
    // 1. 将builder和一个LoadBalancerFeignClient bean实例关联起来
    // 2. 使用一个HystrixTargeter将builder和一个 HardCodedTarget bean实例关联起来
    // 这里 HardCodedTarget 表示对应 url 为 http://test-service/test 的远程服务(可能
    // 包含多个服务方法)
    // 3. 生成最终的feign client 实例 : ReflectiveFeign$FeignInvocationHandler 的动态代理对象，
    // 使用 InvocationHandler ： ReflectiveFeign$FeignInvocationHandler。
    // 每个远程服务方法会对应到一个@FeignClient注解的接口方法上(依据方法上的注解进行匹配)
    return (T) loadBalance(builder, context, new HardCodedTarget<>(this.type,
                                                                   this.name, url));
  }

  // @FeignClient 属性 url 属性被指定的情况 
  // 这种通常是明确指出了服务节点的url的情况，实际上不需要负载均衡
  if (StringUtils.hasText(this.url) && !this.url.startsWith("http")) {
    this.url = "http://" + this.url;
  }
  String url = this.url + cleanPath();
  // 将builder和一个LoadBalancerFeignClient bean实例关联起来
  Client client = getOptional(context, Client.class);
  if (client != null) {
    if (client instanceof LoadBalancerFeignClient) {
      
      client = ((LoadBalancerFeignClient)client).getDelegate();
    }
    builder.client(client);
  }
  // 使用一个HystrixTargeter将builder和一个 HardCodedTarget bean实例关联起来
  Targeter targeter = get(context, Targeter.class);
  // 生成最终的feign client 实例 : ReflectiveFeign$FeignInvocationHandler 的动态代理对象，
  // 使用 InvocationHandler ： ReflectiveFeign$FeignInvocationHandler。        
  return (T) targeter.target(this, builder, context, new HardCodedTarget<>(
    this.type, this.name, url));
}
```

- `FeignClientFactoryBean`#`feign` 创建`feign`客户端Builder

- `FeignClientFactoryBean`#``loadBalance`` 生成具备负载均衡能力的`feign`客户端

  ```java
  // 对builder设置负载均衡客户端，绑定到目标服务端点，构建最终的feign客户端对象
  protected <T> T loadBalance(Feign.Builder builder, FeignContext context,
                              HardCodedTarget<T> target) {
    // 从上下文context获取一个Client，缺省是 LoadBalancerFeignClient 	
    Client client = getOptional(context, Client.class);
    if (client != null) {
      // 将client设置到builder上
      builder.client(client);
      // 从上下文中获取一个 targeter,缺省是一个 HystrixTargeter
      Targeter targeter = get(context, Targeter.class);
      // 上面获取得到的 targeter 会根据 builder 的类型决定如何将 target
      // 绑定到 builder 并设置有关的其他属性和功能,然后生成最终的feign客户端对象
      return targeter.target(this, builder, context, target);
    }
  
    throw new IllegalStateException(
      "No Feign Client for loadBalancing defined. Did you forget to include " +
      "spring-cloud-starter-netflix-ribbon?");
  }
  ```

##### Feign#target

获取缺省的`HystrixTargeter`#`target`交给`Feign`#`target`

```java
// 执行构建并且创建相应的feign客户端实例
public <T> T target(Target<T> target) {
  return build().newInstance(target);
}

 // 构建过程，最终根据各种配置生成一个 ReflectiveFeign 对象
public Feign build() {
  SynchronousMethodHandler.Factory synchronousMethodHandlerFactory =
      new SynchronousMethodHandler.Factory(client, retryer, requestInterceptors, logger,
                                           logLevel, decode404);
  ParseHandlersByName handlersByName =
      new ParseHandlersByName(contract, options, encoder, decoder,
                              errorDecoder, synchronousMethodHandlerFactory);
  return new ReflectiveFeign(handlersByName, invocationHandlerFactory);
}
```

##### ReflectiveFeign #newInstance

 创建最终的feign客户端实例 : 一个 ReflectiveFeign$FeignInvocationHandler 的动态代理对象

```java
@Override
public <T> T newInstance(Target<T> target) {
  Map<String, MethodHandler> nameToHandler = targetToHandlersByName.apply(target);
  Map<Method, MethodHandler> methodToHandler = new LinkedHashMap<Method, MethodHandler>();
  List<DefaultMethodHandler> defaultMethodHandlers = new LinkedList<DefaultMethodHandler>();

  for (Method method : target.type().getMethods()) {
    if (method.getDeclaringClass() == Object.class) {
      continue;
    } else if (Util.isDefault(method)) {
      // 对于每个缺省方法，使用 DefaultMethodHandler 
      DefaultMethodHandler handler = new DefaultMethodHandler(method);
      defaultMethodHandlers.add(handler);
      methodToHandler.put(method, handler);
    } else {
      // 对于每个对应服务功能端点的方法，缺省使用nameToHandler获取的MethodHandler，缺省是 SynchronousMethodHandler
      methodToHandler.put(method, nameToHandler.get(Feign.configKey(target.type(), method)));
    }
  }
  // 创建feign客户端实例 ReflectiveFeign$FeignInvocationHandler,
  // 该对象包含了上面所创建的methodToHandler，用于对应各个开发者定义的@FeignClient接口方法
  InvocationHandler handler = factory.create(target, methodToHandler);
  // 创建feign客户端实例的动态代理对象
  T proxy = (T) Proxy.newProxyInstance(target.type().getClassLoader(),
                                       new Class<?>[] {target.type()}, handler);

  // 将缺省方法处理器绑定到feign客户端实例的动态代理对象上
  for (DefaultMethodHandler defaultMethodHandler : defaultMethodHandlers) {
    defaultMethodHandler.bindTo(proxy);
  }
  return proxy;
}
```

#### FeignInvocationHandler 处理器

参考：https://www.cnblogs.com/crazymakercircle/p/11965726.html

默认的调用处理器 FeignInvocationHandler 是一个相对简单的类，有一个非常重要Map类型成员 dispatch 映射，保存着远程接口方法到MethodHandler方法处理器的映射。在处理远程方法调用的时候，会根据Java反射的方法实例，在dispatch 映射对象中，找到对应的MethodHandler 方法处理器，然后交给MethodHandler 完成实际的HTTP请求和结果的处理。

```java
public class ReflectiveFeign extends Feign {
  //...
  //内部类：默认的Feign调用处理器 FeignInvocationHandler
  static class FeignInvocationHandler implements InvocationHandler {

    private final Target target;
    //方法实例对象和方法处理器的映射
    private final Map<Method, MethodHandler> dispatch;

    //构造函数    
    FeignInvocationHandler(Target target, Map<Method, MethodHandler> dispatch) {
      this.target = checkNotNull(target, "target");
      this.dispatch = checkNotNull(dispatch, "dispatch for %s", target);
    }

    //默认Feign调用的处理
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      	//...
        //首先，根据方法实例，从方法实例对象和方法处理器的映射中，
        //取得 方法处理器，然后，调用 方法处理器 的 invoke(...) 方法
        return dispatch.get(method).invoke(args);
    }
    //...
  }
```

1. 根据Java反射的方法实例，在dispatch 映射对象中，找到对应的MethodHandler 方法处理器；
2. 调用MethodHandler方法处理器的 invoke(...) 方法，完成实际的HTTP请求和结果的处理。

##### SynchronousMethodHandler

MethodHandler 的invoke(…)方法，主要职责是完成实际远程URL请求，然后返回解码后的远程URL的响应结果。Feign提供了默认的 SynchronousMethodHandler 实现类，提供了基本的远程URL的同步请求处理。

1. 首先通 RequestTemplate 请求模板实例，生成远程URL请求实例 request；
2. 然后用自己的 feign 客户端client成员，excecute(…) 执行请求，并且获取 response 响应；
3. 对response 响应进行结果解码。

```java
public Object invoke(Object[] argv) throws Throwable {
  //RequestTemplate 请求模板实例，生成远程URL请求实例 reques
  RequestTemplate template = buildTemplateFromArgs.create(argv);
  Retryer retryer = this.retryer.clone();
  while (true) {
    try {
      //执行请求，获取response并进行结果解码
      return executeAndDecode(template);
    } catch (RetryableException e) {
      retryer.continueOrPropagate(e);
      if (logLevel != Logger.Level.NONE) {
        logger.logRetry(metadata.configKey(), logLevel);
      }
      continue;
    }
  }
}
```

#### 总结

1. `@EnableFeignClients`启用了`feign`客户端的扫描和注册机制，通过`FeignClientsRegistrar`实现`feign`客户端的注册为类型`FeignClientFactoryBean`的`BeanDefinition`

2. 开发过程中通过`@Autowired` Feign客户端，则通过`FeignClientFactoryBean`#`getObject`获取 Feign客户端的`Proxy`实例，`Proxy`实例由`ReflectiveFeign #newInstance`方法创建：创建feign客户端实例 `ReflectiveFeign$FeignInvocationHandler`,该对象有一个`Map<Method, MethodHandler> dispatch`实例变量，保存着方法实例对象和方法处理器的映射。

3. Feign客户端远程调用过程，通过Feign客户端代理`ReflectiveFeign$FeignInvocationHandler`的`dispatch`获取对应的`MethodHandler`执行`invoker`方法。`SynchronousMethodHandler`是`ReflectiveFeign$FeignInvocationHandler` 默认实现：

   1. 首先通 RequestTemplate 请求模板实例，生成远程URL请求实例 request
   2. 对request进行编码
   3. 然后用自己的 feign 客户端client成员，excecute(…) 执行请求，并且获取 response 响应
   4. 对response 响应进行结果解码
   5. 封装为Response Bean

   <img src="/Users/see-bruin/IdeaProjects/bruin/pi/framework/Spring Cloud/images/Feign远程调用过程.jpg" style="zoom:67%;" />



### Client

客户端组件是Feign中一个非常重要的组件，负责端到端的执行URL请求。其核心的逻辑：发送request请求到服务器，并接收response响应后进行解码。feign.Client 类，是代表客户端的顶层接口，只有一个`execute`抽象方法。

1. Client.Default类：默认的feign.Client 客户端实现类，内部使用HttpURLConnnection 完成URL请求处理；
2. ApacheHttpClient 类：内部使用 Apache httpclient 开源组件完成URL请求处理的feign.Client 客户端实现类；
3. OkHttpClient类：内部使用 OkHttp3 开源组件完成URL请求处理的feign.Client 客户端实现类。
4. LoadBalancerFeignClient 类：内部使用 Ribben 负载均衡技术完成URL请求处理的feign.Client 客户端实现类。

#### Client.Default

作为默认的Client 接口的实现类，在Client.Default内部使用JDK自带的HttpURLConnnection类实现URL网络请求。在JKD1.8中，虽然在HttpURLConnnection 底层，使用了非常简单的HTTP连接池技术，但是，其HTTP连接的复用能力，实际是非常弱的，性能当然也很低。

#### ApacheHttpClient

ApacheHttpClient 客户端类的内部，使用 Apache HttpClient开源组件完成URL请求的处理。

- 从代码开发的角度而言，Apache HttpClient相比传统JDK自带的URLConnection，增加了易用性和灵活性，它不仅使客户端发送Http请求变得容易，而且也方便开发人员测试接口。既提高了开发的效率，也方便提高代码的健壮性。
- 从性能的角度而言，Apache HttpClient带有连接池的功能，具备优秀的HTTP连接的复用能力。关于带有连接池Apache HttpClient的性能提升倍数，具体可以参见后面的对比试验。

#### OkHttpClient

OkHttpClient 客户端类的内部，使用OkHttp3 开源组件完成URL请求处理。OkHttp3 开源组件由Square公司开发，用于替代HttpUrlConnection和Apache HttpClient。由于OkHttp3较好的支持 SPDY协议（SPDY是Google开发的基于TCP的传输层协议，用以最小化网络延迟，提升网络速度，优化用户的网络使用体验。），从Android4.4开始，google已经开始将Android源码中的 HttpURLConnection 请求类使用OkHttp进行了替换。也就是说，对于Android 移动端APP开发来说，OkHttp3 组件，是基础的开发组件之一。

#### LoadBalancerFeignClient 

`LoadBalancerFeignClient` 内部使用了 Ribben 客户端负载均衡技术完成URL请求处理。在原理上，简单的使用了delegate包装代理模式：Ribben负载均衡组件计算出合适的服务端server之后，由内部包装 delegate 代理客户端完成到服务端server的HTTP请求；所封装的 delegate 客户端代理实例的类型，可以是 Client.Default 默认客户端，也可以是 ApacheHttpClient 客户端类或OkHttpClient 高性能客户端类，还可以其他的定制的feign.Client 客户端实现类型。

### OpenFeign

OpenFeign是Spring Cloud 在Feign的基础上支持了Spring MVC的注解，如`@RequesMapping`等等。OpenFeign的`@FeignClient`可以解析SpringMVC的`@RequestMapping`注解下的接口，并通过动态代理的方式产生实现类，实现类中做负载均衡并调用其他服务。

OpenFeign的默认的Client是`LoadBalancerFeignClient`，同时也可以配置其他的Client。OpenFeign通过spring.factories的`FeignRibbonClientAutoConfiguration`引入`HttpClientFeignLoadBalancedConfiguration`、`OkHttpFeignLoadBalancedConfiguration`

```java
@ConditionalOnClass({ ILoadBalancer.class, Feign.class })
@Configuration
@AutoConfigureBefore(FeignAutoConfiguration.class)
@EnableConfigurationProperties({ FeignHttpClientProperties.class })
//顺序在这里很重要，last应该是默认值，first应该是可选的
//Order is important here, last should be the default, first should be optional
// see https://github.com/spring-cloud/spring-cloud-netflix/issues/2086#issuecomment-316281653
@Import({ HttpClientFeignLoadBalancedConfiguration.class,
      OkHttpFeignLoadBalancedConfiguration.class,
      DefaultFeignLoadBalancedConfiguration.class })
public class FeignRibbonClientAutoConfiguration {

}



@Configuration
@ConditionalOnClass(ApacheHttpClient.class)
@ConditionalOnProperty(value = "feign.httpclient.enabled", matchIfMissing = true)
class HttpClientFeignLoadBalancedConfiguration {
  
}

@Configuration
@ConditionalOnClass(OkHttpClient.class)
@ConditionalOnProperty(value = "feign.okhttp.enabled")
class OkHttpFeignLoadBalancedConfiguration {
  
}
```

