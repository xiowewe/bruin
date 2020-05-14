### SpringApplication

```java
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
    
}
```

SpringApplication#run()方法包括两部分：SpringApplication的初始化和run()方法执行

#### SpringApplication初始化

```java
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
   this.resourceLoader = resourceLoader;
   Assert.notNull(primarySources, "PrimarySources must not be null");
   //DemoApplication.class作为参数传递并存储为primarySources属性
   this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
   //设置应用类型WebApplicationType：none、servlet、reactive
   this.webApplicationType = WebApplicationType.deduceFromClasspath();
   //设置初始化器Initializer
   setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
   //设置监听器Listener
   setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
   this.mainApplicationClass = deduceMainApplicationClass();
}
```

##### 设置WebApplicationType

```java
//常量
private static final String WEBMVC_INDICATOR_CLASS = "org.springframework.web.servlet.DispatcherServlet";
	private static final String WEBFLUX_INDICATOR_CLASS = "org.springframework.web.reactive.DispatcherHandler";
	private static final String JERSEY_INDICATOR_CLASS = "org.glassfish.jersey.servlet.ServletContainer";

static WebApplicationType deduceFromClasspath() {
   //判断reactive相关class是否存在且DispatcherServlet不存在，则返回reactive类型
   if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
         && !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
      return WebApplicationType.REACTIVE;
   }
   //判断ConfigurableWebApplicationContext.class是否存在
   for (String className : SERVLET_INDICATOR_CLASSES) {
      if (!ClassUtils.isPresent(className, null)) {
         return WebApplicationType.NONE;
      }
   }
   //前两者条件不满足则为servlet
   return WebApplicationType.SERVLET;
}
```

Spring-boot-starter-web 会引入Tomcat和spring-webmvc，spring-webmvc必然会存在SpringMVC的核心类DispatcherServlet，应用最后会返回WebApplicationType.SERVLET

##### 初始化Initializer

读取/META-INF/spring.factories配置文件中Key为：org.springframework.context.ApplicationContextInitializer的value值，遍历并且实例化

```java
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
   ClassLoader classLoader = getClassLoader();
   // 入参type为ApplicationContextInitializer.class
   Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
   //根据names初始化
   List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
   //对实例排序
   AnnotationAwareOrderComparator.sort(instances);
   return instances;
}
```

- SpringFactoriesLoader.loadFactoryNames

  ```java
  public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
  	//根据参数ApplicationContextInitializer.class获取所有的value值
          String factoryTypeName = factoryType.getName();
          return (List)loadSpringFactories(classLoader).getOrDefault(factoryTypeName, Collections.emptyList());
  }
  
  private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
      MultiValueMap<String, String> result = (MultiValueMap)cache.get(classLoader);
      if (result != null) {
          return result;
      } else {
          try {
             //从类路径的META-INF/spring.factories中加载所有默认的自动配置类
              Enumeration<URL> urls = classLoader != null ? classLoader.getResources("META-INF/spring.factories") : ClassLoader.getSystemResources("META-INF/spring.factories");
              LinkedMultiValueMap result = new LinkedMultiValueMap();
  
              while(urls.hasMoreElements()) {
                  URL url = (URL)urls.nextElement();
                  UrlResource resource = new UrlResource(url);
                  Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                  Iterator var6 = properties.entrySet().iterator();
  								
                 //循环获取所有key的value值，存储在LinkedMultiValueMap中
                  while(var6.hasNext()) {
                      Entry<?, ?> entry = (Entry)var6.next();
                      String factoryTypeName = ((String)entry.getKey()).trim();
                      String[] var9 = StringUtils.commaDelimitedListToStringArray((String)entry.getValue());
                      int var10 = var9.length;
  
                      for(int var11 = 0; var11 < var10; ++var11) {
                          String factoryImplementationName = var9[var11];
                          result.add(factoryTypeName, factoryImplementationName.trim());
                      }
                  }
              }
  
              cache.put(classLoader, result);
              return result;
          } catch (IOException var13) {
              throw new IllegalArgumentException("Unable to load factories from location [META-INF/spring.factories]", var13);
          }
      }
  }
  ```

- createSpringFactoriesInstances

  ```java
  private <T> List<T> createSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes,
        ClassLoader classLoader, Object[] args, Set<String> names) {
     List<T> instances = new ArrayList<>(names.size());
     //遍历获取的value值
     for (String name : names) {
        try {
           Class<?> instanceClass = ClassUtils.forName(name, classLoader);
          //确认被加载类是ApplicationContextInitializer 的子类
          Assert.isAssignable(type, instanceClass);
          //获取构造器
           Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);
           //通过构造器反射实例化对象
           T instance = (T) BeanUtils.instantiateClass(constructor, args);
           instances.add(instance);
        }
        catch (Throwable ex) {
           throw new IllegalArgumentException("Cannot instantiate " + type + " : " + name, ex);
        }
     }
     return instances;
  }
  ```

##### 初始化Listener

​	和初始化Initializer如出一辙，入参改为ApplicationListener.class，初始化的ApplicationListener将贯穿整个SpringBoot的生命周期



------



#### SpringApplication#run()

```java
public ConfigurableApplicationContext run(String... args) {
   //计时工具
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
		configureHeadlessProperty();
    //1、获取并启动监听器（包括SpringApplication初始化的所有监听器）
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting();
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
      //2、根据listeners和参数准备环境
			ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
			configureIgnoreBeanInfo(environment);
      //打印springboot字体
			Banner printedBanner = printBanner(environment);
      //3、创建Spring容器
			context = createApplicationContext();
			exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
      //4、Spring容器前置处理
			prepareContext(context, environment, listeners, applicationArguments, printedBanner);
      //5、刷新容器
			refreshContext(context);
      //6、Spring容器后值处理
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
			}
      //7、结束监听器
			listeners.started(context);
      //8、执行Runners
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}
  //省略。。。
}
```



##### 获取启动监听器

- 获取监听器

  - EventPublishingRunListener构造方法

    添加所有的监听器，包括前面初始化的所有监听器 

    - addApplicationListener方法

      添加监听器方法，把所有添加的监听器保存在AbstractApplicationEventMulticaster 内部对象ListenerRetriever 的applicationListeners 属性中

  - SpringApplicationRunListener 接口方法

    不同接口在spring boot初始化过程中不同状态时执行

    - 区别SpringApplicationRunListeners类和SpringApplicationRunListener方法
    - 自定义SpringApplicationRunListener ，必须提供一个包含参数（SpringApplication application, String[] args）的构造方法

- 启动监听器

  - SpringApplicationRunListener#starting方法

    创建ApplicationStartingEvent，并根据spring boot启动的状态触发不同的event

##### 准备环境

```java
private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,ApplicationArguments applicationArguments) {
    //根据WebApplicationType类型创建或获取相应的ConfigurableEnviroment
    ConfigurableEnvironment environment = getOrCreateEnvironment();
    //配置ConfigurableEnviroment
    configureEnvironment(environment, applicationArguments.getSourceArgs());
    ConfigurationPropertySources.attach(environment);
    //发布环境已准备的监听
    listeners.environmentPrepared(environment);
    bindToSpringApplication(environment);
    if (!this.isCustomEnvironment) {
        environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
                                                                                               deduceEnvironmentClass());
    }
    ConfigurationPropertySources.attach(environment);
    return environment;
}
```

- 获取相应的ConfigurableEnviroment

  /根据WebApplicationType类型创建或获取相应的ConfigurableEnviroment

- 配置ConfigurableEnviroment

  添加和设置sources or profiles配置

- environmentPrepared

  监听器触发environmentPrepared方法，激活触发ApplicationEnvironmentPreparedEvent类型监听器，例如**ConfigFileApplicationListener**，主要用来处理项目配置。项目中的 properties 和yml文件都是其内部类所加载。首先还是会去读spring.factories 文件，获取执行完配置的后处理类，ConfigFileApplicationListener会执行该类本身的逻辑。

##### 创建Spring容器

##### Spring容器前置处理

##### 容器刷新

##### Spring容器后置处理

##### 结束监听器

##### 执行Runners

https://juejin.im/post/5c2af8915188252a94130422

