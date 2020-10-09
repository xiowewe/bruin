### AbstractApplicationContext#refresh()

```java
public void refresh() throws BeansException, IllegalStateException {
   synchronized (this.startupShutdownMonitor) {
      // Prepare this context for refreshing.
      prepareRefresh();
      // Tell the subclass to refresh the internal bean factory.
      ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
      // Prepare the bean factory for use in this context.
      prepareBeanFactory(beanFactory);
      try {
         // Allows post-processing of the bean factory in context subclasses.
         postProcessBeanFactory(beanFactory);
         // Invoke factory processors registered as beans in the context.
         invokeBeanFactoryPostProcessors(beanFactory);
         // Register bean processors that intercept bean creation.
         registerBeanPostProcessors(beanFactory);
         // Initialize message source for this context.
         initMessageSource();
         // Initialize event multicaster for this context.
         initApplicationEventMulticaster();
         // Initialize other special beans in specific context subclasses.
         onRefresh();
         // Check for listener beans and register them.
         registerListeners();
         // Instantiate all remaining (non-lazy-init) singletons.
         finishBeanFactoryInitialization(beanFactory);

         // Last step: publish corresponding event.
         finishRefresh();
      }catch (BeansException ex) {
         if (logger.isWarnEnabled()) {
            logger.warn("Exception encountered during context initialization - " +
                  "cancelling refresh attempt: " + ex);
         }

         // Destroy already created singletons to avoid dangling resources.
         destroyBeans();
         // Reset 'active' flag.
         cancelRefresh(ex);
         // Propagate exception to caller.
         throw ex;
      }finally {
         // Reset common introspection caches in Spring's core, since we
         // might not ever need metadata for singleton beans anymore...
         resetCommonCaches();
      }
   }
}
```

#### prepareRefresh

Spring应用上下文启动准备阶段

1. 设置启动时间-startupDate
2. 设置状态标志-close(false)、active(true)
3. 初始化`PropertySource`-`initPropertySource`
4. 检验`Environment`中必须属性
5. 初始化监听器集合
6. 初始化早期Spring事件集合

#### obtainFreshBeanFactory

BeanFactory创建阶段

1. 刷新Spring上下文底层`BeanFactory`-`refreshBeanFactory`
   1. 销毁或关闭`Beanfactory`，如果存在的话
   2. 创建`BeanFactory`
   3. 设置`BeanFactory` id
   4. 定制`BeanFactory`-`customerBeanFactory`
      1. 是否允许重复定义`allowBeanDefinitionOverriding`
      2. 是否允许循环依赖`allowCircularReferences`
   5. 加载`BeanDefinition`-`loadBeanDefinitions`
   6. 关联新建`BeanFactory`到Spring上下文中
2. 返回Spring上下文底层`BeanFactory`-`getBeanFactory`

#### prepareBeanFactory

BeanFactory准备阶段

1. 关联`ClassLoader`
2. 设置Bean表达式处理器
3. 添加`PropertyEditorRegister`实现`ResourceEditorRegister`
4. 添加Aware回调接口`BeanPostProcessor`实现`ApplicationContextAwareProcessor`
5. 忽略Aware回调接口作为依赖注入接口
6. 注册`ResolvableDependency`对象-`BeanFactory`、`ResourceLoader`、`ApplicationEventPublisher`以及`ApplicationContext`
7. 注册`ApplicationListenerDetector`对象
8. 注册`LoadTimeWeaverAwareProcessor`
9. 注册单例对象-`Environment`、Java System Properties以及OS环境变量

#### postProcessBeanFactory

允许在上下文子类中对bean工厂进行后处理，模版方法用于子类对beanFactory进行后置处理。例如`AbstractRefreshableWebApplicationContext`：

1. 注册ServletContextAwareProcessor
2. 忽略`ServletContextAware`、`ServletConfigAware`
3. 注册Web环境、包括request、session、golableSession、application
4. 注册servletContext、contextParameter、contextAttribute、servletConfig单例

#### invokeBeanFactoryPostProcessors

实例化并调用注册的`BeanFactoryPostProcessor` Bean，遵循显示顺序（PriorityOrdered、Ordered）

1. 实例化并调用注册的`BeanFactoryPostProcessor`-实际委托给`PostProcessorRegistrationDelegate`#`invokeBeanFactoryPostProcesser`

   1. BeanFactoryPostProcessor`的处理分为`BeanDefinitionRegistryPostProcessor`类型的特殊处理和普通`BeanFactoryPostProcessor`的处理，同时两中都包括**硬编码**、**配置注入**两种型式的后处理器。`
   2. **硬编码**的形式通过`AbstractApplicationContext`#`addBeanFactoryPostProcessor` 添加，存放在`beanFactoryPostProcessors`中。
   3. `BeanDefinitionRegistryPostProcessor`类型的后处理器相比普通类型后处理器需要先执行postProcessBeanDefinitionRegistry接口方法。

   代码执行逻辑：

   1. 调用【硬编码】`BeanDefinitionRegistryPostProcessor`类型`postProcessBeanDefinitionRegistry`接口
   2. 调用【配置方式】`BeanDefinitionRegistryPostProcessor`类型 `postProcessBeanDefinitionRegistry`接口
   3. 调用【硬编码】`BeanDefinitionRegistryPostProcessor`类型`postProcessBeanFactory`接口
   4. 调用【硬编码】 普通类型	`postProcessBeanFactory`接口
   5. 调用【配置方式】 普通类型和`BeanDefinitionRegistryPostProcessor`类型	`postProcessBeanFactory`接口

2. 注册`LoadTimeWeaverAwareProcessor`

3. 设置`ContextTypeMatchClassLoader`

#### registerBeanPostProcessors

注册`BeanPostProcessor`，遵循显示顺序（PriorityOrdered、Ordered）。-实际委托给`PostProcessorRegistrationDelegate`#`invokeBeanPostProcessers`

1. 注册`BeanPostProcessorChecker`（`PostProcessorRegistrationDelegate`的内部类）
2. 注册PriorityOrdered类型的`BeanPostProcessor`
3. 注册Ordered类型的`BeanPostProcessor`
4. 注册普通的`BeanPostProcessor`
5. 注册`MergedBeanDefinitionPostProcessor`
6. 注册`ApplicationListenerDetector`对象

#### initMessageSource

#### initApplicationEventMulticaster

#### onRefresh

模板方法以添加特定于上下文的刷新工作。 在实例化单例之前可以调用特殊bean的初始化。例如在 SpringBoot 中主要用于启动内嵌的 Web 服务器：`ServletWebServerApplicationContext`、`ReactiveWebServerApplicationContext`

#### registerListeners

#### finishBeanFactoryInitialization

#### finishRefresh