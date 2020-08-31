public void refresh() throws BeansException, IllegalStateException {
	synchronized (this.startupShutdownMonitor) {
		// Prepare this context for refreshing.
		// 准备更新上下文，设置启动时间，标记活动标志，初始化配置文件中的占位符
		prepareRefresh();

		// Tell the subclass to refresh the internal bean factory.
		//一、初始化BeanFactory
		// 1、createBeanFactory();
		//		创建DefaultListableBeanFactory
		// 2、customizeBeanFactory(beanFactory);
		//		自定义此上下文使用的内部BeanFactory，可设置 allowBeanDefinitionOverriding（BeanDefinition 是否可覆盖，BeanDefinition注册阶段常用）
		//		allowCircularReferences（是否可以循环依赖）
		// 3、loadBeanDefinitions(beanFactory); 
		//		通过 XmlBeanDefinitionReader 解析 xml 文件，解析封装信息到 BeanDefinition，并通过 DefaultListableBeanFactory 将其 register 到
		//		BeanFactory 中以 beanName为key将 BeanDefinition 存到 DefaultListableBeanFactory#beanDefinitionMap 中
		ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

		// Prepare the bean factory for use in this context.
		// 二、准备 BeanFactory ,填充各种功能
		// 1、设置 BeanFactory 的ClassLoader
		// 2、配置 BeanFactoryAware接口（如：ApplicationContextAware、MessageSourceAware）
		// 3、register 默认的环境bean
		prepareBeanFactory(beanFactory);

		try {
			// Allows post-processing of the bean factory in context subclasses.
			// 三、子类覆盖做拓展处理
			//		方法为空实现，留给子类做扩展（如：AbstractRefreshableWebApplicationContext）
			postProcessBeanFactory(beanFactory);

			// Invoke factory processors registered as beans in the context.
			// 四、激活BeanFactory的各种postProcessor
			// 1、PostProcessorRegistrationDelegate委托类激活postProcessor
			// 比如：configurationClass 处理器：ConfigurationClassPostProcessor
			// 2、ConfigurationClassPostProcessor实现BeanDefinitionRegistryPostProcessor的postProcessBeanFactory(factory) 方法，
     		//   最后交给processConfigBeanDefinitions(registry) 方法进行configurationClass BeanDefinition处理
     		// 3、ConfigurationClassParser解析构建配置类（@Component、@PropertySource、@ComponentScan、@Import、@ImportSource、@Bean等注解的解析）
     		// 4、ConfigurationClassBeanDefinitionReader.loadBeanDefinitions() 注册解析构建好的configurationClass BeanDefinition
			// 扩展例如：MyBatis MapperScannerConfigurer 和 MapperScannerRegistrar，扫描Mapper register 到 BeanFactory 中
			//
			// 另外ConfigurationClassPostProcessor是什么时候到Spring容器中的呢？答案是AnnotationConfigApplicationContext容器（SpringBoot默认创建的就是改容器）
			// 在创建的时候 同时会创建 AnnotatedBeanDefinitionReader 实例，继而通过 AnnotationConfigUtils 注册Spring内置的configurationClass BeanDefinition
			// 其中就包括 ConfigurationClassPostProcessor、AutowiredAnnotationBeanPostProcessor、CommonAnnotationBeanPostProcessor等，
			// AutowiredAnnotationBeanPostProcessor 就是我们开发中常用 @Autowired 的具体实现原理所在，在Bean 的创建doCreateBean()方法中的populateBean()
			// 属性装配中调用后置处理器 AutowiredAnnotationBeanPostProcessor的 postProcessProperties() 方法，进行 @Autowired 注解的解析
			invokeBeanFactoryPostProcessors(beanFactory);

			// Register bean processors that intercept bean creation.
			// 五、注册BeanPostProcessor，只是注册，正真调用在getBean的时候
			// BeanPostProcessor和前面的BeanFactoryPostProcessor不同
			//		1、BeanFactoryPostProcessor是在spring容器加载了bean的定义文件之后，在bean实例化之前执行的，根据需要可以配置多个BeanFactoryPostProcessor，
			//		  还可以通过设置'order'属性来控制BeanFactoryPostProcessor的执行次序。如：ConfigurationClassPostProcessor
			//		  接口：postProcessBeanFactory(ConfigurableListableBeanFactory)
			//		2、BeanPostProcessor是在spring容器加载了bean的定义文件并且实例化bean之后执行的，执行顺序是在BeanFactoryPostProcessor之后。如：
			//		  AutowiredAnnotationBeanPostProcessor
			//		  接口： postProcessBeforeInitialization 和 postProcessAfterInitialization
			//
			// 这时 Bean 还没初始化，下面的 finishBeanFactoryInitialization 才是真正的初始化方法
			registerBeanPostProcessors(beanFactory);

			// Initialize message source for this context.
			// 六、初始化《Message源，不同语言消息的国际化处理
			// 初始化当前 ApplicationContext 的 MessageSource，解析消息的策略接口，用于支持消息的国际化和参数化
			// Spring 两个开箱即用的实现 ResourceBundleMessageSource 和 ReloadableResourceBundleMessageSource
			initMessageSource();

			// Initialize event multicaster for this context.
			// 七、初始化当前 ApplicationContext 的事件广播器
			initApplicationEventMulticaster();

			// Initialize other special beans in specific context subclasses.
			// 八、留给子类处理，典型的模板方法
			// 子类可以在实例化 bean 之前，做一些初始化工作，SpringBoot 会在这边启动 Web 服务
			onRefresh();

			// Check for listener beans and register them.
			// 九、向 initApplicationEventMulticaster() 初始化的 applicationEventMulticaster 注册事件监听器，就是实现 ApplicationListener 接口类
			// 观察者模式，例如实现了 ApplicationEvent，通过 ApplicationEventPublisher#publishEvent()，可以通知到各个 ApplicationListener#onApplicationEvent
			registerListeners();

			// Instantiate all remaining (non-lazy-init) singletons.
			// 十、初始化所有的 singletons bean（lazy-init 的除外），Spring bean 初始化核心方法
			// 1、初始化交给DefaullistableBeanFactory.preInstantiateSingletons 
			// 		1.遍历 beanDefinitionNames 的副本，非抽象且非延迟的单例 RootBeanDefinition 调用个getBean(beanName)
			//		2、SmartInitializingSingleton，Bean实例化后执行自定义初始化 (Ribbon 使用 SmartInitializingSingleton 对定制个性化的 RestTemplate)
			// 2、getBean(beanName)
			//		1.getSigleton(beanName); 缓存中获取单例对象
			//		2.getObjectForBeanInstance(..); 缓存中存在单例对象，从bean的实例中获取对象
			//		3.getMergedLocalBeanDefinition(beanName); 获取RootBeanDefinition，此时子类获取父类的属性
			//		4.getSingleton(beanName, ObjectFactory); 通过singletonFactory.getObject() 获取bean对象，三级缓存：singletonObjects、singletonFactories、
			//		  earlySingletonObjects ，解决循环依赖
			//		5.createBean(beanName, mbd, args); 准备创建bean：prepareMethodOverrides()处理overrides属性，resolveBeforeInstantiation()
			//		6.resolveBeforeInstantiation 给BeanPostProcessor一个返回代理而不是目标bean实例的机会
			//		6.doCreateBean(beanName, mbd, args);
			//			6.1 createBeanInstance(); 根据bean使用的策略进行实例化：instantiateUsingFactoryMethod()工厂方法、autowireConstructor构造函数自动注入、
			//				getInstantiationStrategy()获取策略实例化
			//			6.2 populateBean(beanName, RootBeanDefinition, BeanWrapper); 属性注入
			//				6.2.1 bean 初始化后后置处理器，在属性配置前最后一次机会改变Bean。可实现InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation接口，
			//					  做Bean实例化的后置操作且return false，后续操纵不执行
			//				6.2.2 自动注入byname、byType
			//				6.2.3 应用InstantiationAwareBeanPostProcessor#postProcessorPropertyValues方法对属性的再次处理，包括AutowiredAnnotationBeanPostProcessor进行字段注入等
			//				6.2.4 applyPropertyValues() 将propertyValues填充到bean中
			//			6.3 initializeBean(beanName,  bean, RootBeanDefinition) 实例化
			//				6.3.1 invokeAwareMethods(beanName, bean); 激活Aware方法，实现这些接口的Bean在初始化之后可以获得一些资源，比如BeanNameAware 可以获取BeanName，BeanFactoryAware
			//					  注入BeanFactory实例，以及ApplicationContextAware 会注入ApplicationContext实例等。接口实现的依次顺序:（该方法实现前三者，后者的实现在Bean初始化前置处理其中实现）
			//					  BeanNameAware、BeanClassLoaderAware、BeanFactoryAware、EnviromentAware、EmbeddedValueResolverAware、ResourceLoarderAware、ApplicationEnventPublisherAware、
			//					  MessageSourceAware、ApplicationContextAware
			//				6.3.2 applyBeanPostProcessorsBeforeInitializationBean实例化前置处理器处理所有BeanPostProcessor#postProcessBeforeInitialization接口的实现，前面后几个Aware接口就是
			//					  通过ApplicationContextAwareProcessor 处理器进行处理实现的
			//				6.3.3 invokeInitMethods(beanName, wrappedBean, mbd); 激活初始化方法限制性InitializingBean#afterPropertiesSet 方法逻辑，后执行init—method自定义方法，而@PostConstrct 
			//					  是通过CommonAnnotationBeanPostProcessor 初始化前置处理器实现，所以三者的执行顺序是：@PostConstrct --》 InitializingBean#afterPropertiesSet --》 init—method
			//				6.3.4 applyBeanPostProcessorsAfterInitialization 实例化后置处理器
			//			6.4 registerDisposableBeanIfNecessary 注册销毁Bean
			//				类似Bean的初始化，同样有Destroyable#destroy接口、@PreDestroy、destroy—method 自定义方法，执行顺序：@PreDestroy--》 Destroyable#destroy --》 destroy—method
			finishBeanFactoryInitialization(beanFactory);

			// Last step: publish corresponding event.
			// 十一、ApplicationEventPublisher#publishEvent() 初始化完成（ContextRefreshedEvent）事件
			finishRefresh();
		}

		catch (BeansException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Exception encountered during context initialization - " +
						"cancelling refresh attempt: " + ex);
			}

			// Destroy already created singletons to avoid dangling resources.
			// 十二、destroy 已经创建的 singleton 避免占用资源
			destroyBeans();

			// Reset 'active' flag.
			// 十三、重置'有效'标志
			cancelRefresh(ex);

			// Propagate exception to caller.
			throw ex;
		}

		finally {
			// Reset common introspection caches in Spring's core, since we
			// might not ever need metadata for singleton beans anymore...
			// 十四、重置Spring核心中的常见内省缓存，因为可能不再需要单例bean的元数据了...
			resetCommonCaches();
		}
	}
}