#### AnnotationConfigApplicationContext容器创建过程

##### AnnotationConfigApplicationContext

​	管理注解bean容器，继承GernericApplicationContext（内部定义了一个DefaultlistableBeanFactory），实现BeanDefinitionRegistry接口，可以注册config class 的BeanDefinition

1. 构造函数

   ```java
   /**
    * Create a new AnnotationConfigApplicationContext that needs to be populated
    * through {@link #register} calls and then manually {@linkplain #refresh refreshed}.
    */
   public AnnotationConfigApplicationContext() {
       //创建AnnotatedBeanDefinitionReader，默认注册6个处理器
   	this.reader = new AnnotatedBeanDefinitionReader(this);
   	this.scanner = new ClassPathBeanDefinitionScanner(this);
   }
   
   /**
    * Create a new AnnotationConfigApplicationContext with the given DefaultListableBeanFactory.
    * @param beanFactory the DefaultListableBeanFactory instance to use for this context
    */
   public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
   	super(beanFactory);
   	this.reader = new AnnotatedBeanDefinitionReader(this);
   	this.scanner = new ClassPathBeanDefinitionScanner(this);
   }
   
   /**
    * Create a new AnnotationConfigApplicationContext, deriving bean definitions
    * from the given component classes and automatically refreshing the context.
    * @param componentClasses one or more component classes &mdash; for example,
    * {@link Configuration @Configuration} classes
    */
   public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
   	this();
   	//注册从config 配置类
   	register(componentClasses);
   	//刷新上下文
   	refresh();
   }
   ```

   

2. AnnotationConfigApplicationContext 注册config配置类

   ```java
   //注册核心代码
   private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
   			@Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
   			@Nullable BeanDefinitionCustomizer[] customizers) {
   
   		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
   		//@Condition条件是否跳过注册
   		if (this.@Condition条件是否跳过注册Evaluator.shouldSkip(abd.getMetadata())) {
   			return;
   		}
   		//设置回调
   		abd.setInstanceSupplier(supplier);
   		//解析Bean的作用域，@Scop（默认singleton）
   		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
   		abd.setScope(scopeMetadata.getScopeName());
   		//生成beanName
   		String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));
   		//解析通用注解：Lazy, primary DependsOn, Role ,Description）
   		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
   		if (qualifiers != null) {
   			for (Class<? extends Annotation> qualifier : qualifiers) {
   				if (Primary.class == qualifier) {
   					abd.setPrimary(true);
   				}
   				else if (Lazy.class == qualifier) {
   					abd.setLazyInit(true);
   				}
   				else {
   					abd.addQualifier(new AutowireCandidateQualifier(qualifier));
   				}
   			}
   		}
       	//自定义bean注册
   		if (customizers != null) {
   			for (BeanDefinitionCustomizer customizer : customizers) {
   				customizer.customize(abd);
   			}
   		}
   		//bean定义信息封装一个BeanDefinitionHolder：beanname和BeanDefinition的映射
   		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
   		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
       	//BeanDefinition注册，简单理解为beanDefinition保存到beanDefinitionMap中
   		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
   	}
   ```

##### AnnotationBeanDefinitionReader

1. ​	构造函数

   ```java
   public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
   	this(registry, getOrCreateEnvironment(registry));
   }
   
   
   public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
   	Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
   	Assert.notNull(environment, "Environment must not be null");
   	this.registry = registry;
   	this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);
       //注册6个注解处理器
   	AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
   }
   ```

2. AnnotationConfigUtils.registerAnnotationConfigProcessors

   ```java
   public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
   			BeanDefinitionRegistry registry, @Nullable Object source) {
   
   	//...省略部分代码...
   
   	Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<>(8);
   
       //ConfigurationClassPostProcessor处理器
   	if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
   		RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
   		def.setSource(source);
   		beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
   	}
   	//AutowiredAnnotationBeanPostProcessor处理器（@Autowired注入处理器）
   	if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
   		RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
   		def.setSource(source);
   		beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
   	}
   
   	// Check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor.
       //CommonAnnotationBeanPostProcessor处理器
   	if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
   		RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
   		def.setSource(source);
   		beanDefs.add(registerPostProcessor(registry, def, COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
   	}
   
   	// Check for JPA support, and if present add the PersistenceAnnotationBeanPostProcessor.
       //PersistenceAnnotationProcessor 处理器
   	if (jpaPresent && !registry.containsBeanDefinition(PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
   		RootBeanDefinition def = new RootBeanDefinition();
   		try {
   			def.setBeanClass(ClassUtils.forName(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME,
   					AnnotationConfigUtils.class.getClassLoader()));
   		}
   		catch (ClassNotFoundException ex) {
   			throw new IllegalStateException(
   					"Cannot load optional framework class: " + PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, ex);
   		}
   		def.setSource(source);
   		beanDefs.add(registerPostProcessor(registry, def, PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME));
   	}
   
       //EventListenerMethodProcessor 处理器
   	if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
   		RootBeanDefinition def = new RootBeanDefinition(EventListenerMethodProcessor.class);
   		def.setSource(source);
   		beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_PROCESSOR_BEAN_NAME));
   	}
   
       //EventListenerFactory
   	if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
   		RootBeanDefinition def = new RootBeanDefinition(DefaultEventListenerFactory.class);
   		def.setSource(source);
   		beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_FACTORY_BEAN_NAME));
   	}
   
   	return beanDefs;
   }
   ```

   - ConfigurationClassPostProcessor是一个BeanFactory和BeanDefinitionRegistry处理器，

     BeanDefinitionRegistry处理方法能处理@Configuration等注解，ConfigurationClassPostProcessor处理@Configuration，@Import，@ImportResource和类内部的@Bean。

     ConfigurationClassPostProcessor类继承了BeanDefinitionRegistryPostProcessor，它和BeanPostProcessor不同，BeanPostProcessor只是在Bean初始化的时候有个钩子让我们加入一些自定义操作；在Mybatis与Spring的整合中，就利用到了BeanDefinitionRegistryPostProcessor来对Mapper的BeanDefinition进行了后置的自定义处理。

   - AutowiredAnnotationBeanPostProcessor是一个**BeanPostProcessor**（注意和BeanFactoryPostProcessor的区别），来处理@Autowired注解和@Value注解

   - CommonAnnotationBeanPostProcessor提供对JSR-250规范注解的支持@javax.annotation.Resource、@javax.annotation.PostConstruct和@javax.annotation.PreDestroy等的支持

   - EventListenerMethodProcessor提供@PersistenceContext的支持

   - EventListenerMethodProcessor提供@ EventListener  的支持

##### ClassPathBeanDefinitionScanner

 ClassPathBeanDefinitionScanner是一个扫描指定类路径中注解Bean定义的扫描器，在它初始化的时候，会初始化一些需要被扫描的注解，初始化用于加载包下的资源的Loader

1. 构造函数

   ```java
   public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,Environment environment, @Nullable ResourceLoader resourceLoader) {
   
       Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
       this.registry = registry;
   
       if (useDefaultFilters) {
       	registerDefaultFilters();
       }
       setEnvironment(environment);
       //ResourcePatternResolver，MetadataReaderFactory和CandidateComponentsIndex设定初始值
       setResourceLoader(resourceLoader);
   }
   ```

   

2. registerDefaultFilters

   ​       为includeFilters加入了三个AnnotationTypeFilter，它是TypeFilter的一个实现，用于判断类的注解修饰型是否满足要求。从中可知通过 @Component、@javax.annotation.ManagedBean 和 @javax.inject.Named 以及标记了这些 Annotation 的新 Annotation 注解过的 Java 对象即为 Spring 框架通过 Annotation 配置的默认规则。注意@service，@controller等都继承了@component，是符合规则的

   ```java
   protected void registerDefaultFilters() {
       //加入 @Component
   	this.includeFilters.add(new AnnotationTypeFilter(Component.class));
   	ClassLoader cl = ClassPathScanningCandidateComponentProvider.class.getClassLoader();
   	try {
           //加入 @ManagedBean
   		this.includeFilters.add(new AnnotationTypeFilter(
   				((Class<? extends Annotation>) ClassUtils.forName("javax.annotation.ManagedBean", cl)), false));
   		logger.trace("JSR-250 'javax.annotation.ManagedBean' found and supported for component scanning");
   	}
   	catch (ClassNotFoundException ex) {
   	}
   	try {
           //加入 @Named
   		this.includeFilters.add(new AnnotationTypeFilter(
   				((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Named", cl)), false));
   		logger.trace("JSR-330 'javax.inject.Named' annotation found and supported for component scanning");
   	}
   	catch (ClassNotFoundException ex) {
   	}		
   }
   ```


#### Annotation BeanDefinition注册

```
调用链路：
AbstractApplicationContext.refresh() -->
	invokeBeanFactoryPostProcessors() -->
		PostProcessorRegistrationDelegate.
		invokeBeanFactoryPostProcessors() -->
				ConfigurationClassPostProcessor.
				processConfigBeanDefinitions() -->
					ConfigurationClassBeanDefinitionReader.
					loadBeanDefinitions() -->
						DefaultlistableBeanFactory.
                        registerBeanDefinition()
			
```

##### **PostProcessorRegistrationDelegate**

​    PostProcessorRegistrationDelegate是AbstractApplicationContext委托执行post processors任务的工具类

```java
//执行 BeanFactoryPostProcessor
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors){
    //..........
}

//注册 BeanPostProcessor
public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
    //..........
}
```

invokeBeanFactoryPostProcessors()具体实现逻辑（搞不太清楚里面的逻辑）

```java
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

	Set<String> processedBeans = new HashSet<>();

	if (beanFactory instanceof BeanDefinitionRegistry) {
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
		//记录常规 BeanFactoryPostProcessor
		List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
        //记录 BeanDefinitionRegistryPostProcessor
		List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

        //遍历所有 BeanFactoryPostProcessor分成两组
        //1、BeanDefinitionRegistryPostProcessor
        //2、BeanFactoryPostProcessor，不执行处理逻辑
		for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
			if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
				BeanDefinitionRegistryPostProcessor registryProcessor =
						(BeanDefinitionRegistryPostProcessor) postProcessor;
				registryProcessor.postProcessBeanDefinitionRegistry(registry);
				registryProcessors.add(registryProcessor);
			}
			else {
				regularPostProcessors.add(postProcessor);
			}
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		// Separate between BeanDefinitionRegistryPostProcessors that implement
		// PriorityOrdered, Ordered, and the rest.
        //记录当前正要被执行的BeanDefinitionRegistryPostProcessor
		List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

		// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
        // 1. 对 Bean形式 BeanDefinitionRegistryPostProcessor + PriorityOrdered 的调用
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
				processedBeans.add(ppName);
			}
		}
		sortPostProcessors(currentRegistryProcessors, beanFactory);
        // Bean形式存在的 BeanDefinitionRegistryPostProcessor 也添加到 registryProcessors 中
		registryProcessors.addAll(currentRegistryProcessors);
        
        // 对bean形式存在的 BeanDefinitionRegistryPostProcessor 执行其对	
		// BeanDefinitionRegistry的postProcessBeanDefinitionRegistry()
					invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
		currentRegistryProcessors.clear();

		// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
        // 2. 对 Bean形式 BeanDefinitionRegistryPostProcessor + Ordered的调用
		postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
		for (String ppName : postProcessorNames) {
			if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
				currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
				processedBeans.add(ppName);
			}
		}
		sortPostProcessors(currentRegistryProcessors, beanFactory);
		registryProcessors.addAll(currentRegistryProcessors);
		invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
		currentRegistryProcessors.clear();

		// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
        // 3. 对 Bean形式 BeanDefinitionRegistryPostProcessor , 并且未实现
		// PriorityOrdered或者Ordered接口进行处理，直到没有未被处理的
		boolean reiterate = true;
		while (reiterate) {
			reiterate = false;
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
					reiterate = true;
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();
		}

		// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
        // 因为BeanDefinitionRegistryPostProcessor继承自BeanFactoryPostProcessor,所以这里
		// 也对所有 BeanDefinitionRegistryPostProcessor 调用其方法 postProcessBeanFactory()
		invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
        // 对所有常规 BeanFactoryPostProcessor 调用其方法 postProcessBeanFactory()
		invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
	}

	else {
		// Invoke factory processors registered with the context instance.
		invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
	}
    
    
    // 以上逻辑执行了所有参数传入的和以bean定义方式存在的BeanDefinitionRegistryPostProcessor,
	// 也执行了所有参数传入的BeanFactoryPostProcessor, 但是尚未处理所有以bean定义方式存在的
	// BeanFactoryPostProcessor, 下面的逻辑处理这部分 BeanFactoryPostProcessor.
    

	// Do not initialize FactoryBeans here: We need to leave all regular beans
	// uninitialized to let the bean factory post-processors apply to them!
	String[] postProcessorNames =
			beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

	// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
	// Ordered, and the rest.
	List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
	List<String> orderedPostProcessorNames = new ArrayList<>();
	List<String> nonOrderedPostProcessorNames = new ArrayList<>();
	for (String ppName : postProcessorNames) {
		if (processedBeans.contains(ppName)) {
			// skip - already processed in first phase above
		}
		else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
			priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
		}
		else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
			orderedPostProcessorNames.add(ppName);
		}
		else {
			nonOrderedPostProcessorNames.add(ppName);
		}
	}

	// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
	sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
	invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

	// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
	List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
	for (String postProcessorName : orderedPostProcessorNames) {
		orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
	}
	sortPostProcessors(orderedPostProcessors, beanFactory);
	invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

	// Finally, invoke all other BeanFactoryPostProcessors.
	List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
	for (String postProcessorName : nonOrderedPostProcessorNames) {
		nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
	}
	invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

	// Clear cached merged bean definitions since the post-processors might have
	// modified the original metadata, e.g. replacing placeholders in values...
	beanFactory.clearMetadataCache();
}
```



##### ConfigurationClassPostProcessor

ConfigurationClassPostProcessor后置处理器的处理入口为实现**BeanDefinitionRegistryPostProcessor**的postProcessBeanDefinitionRegistry()方法。其主要使用了ConfigurationClassParser配置类解析器解析@Configuration配置类上的诸如@ComponentScan、@Import、@Bean等注解，并尝试发现所有的配置类；还使用了**ConfigurationClassBeanDefinitionReader**注册所发现的所有配置类中的所有Bean定义；结束执行的条件是所有配置类都被发现和处理，相应的bean定义注册到容器。

processConfigBeanDefinitions()源码实现逻辑

```java
public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
	List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
	String[] candidateNames = registry.getBeanDefinitionNames();
	//遍历容器所有的BeanDefinition
	for (String beanName : candidateNames) {
		BeanDefinition beanDef = registry.getBeanDefinition(beanName);
		if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
			}
		}
        //检查BeanDefinition是否为‘完全配置类’，放入集合后续处理
		else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
			configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
		}
	}

	// Return immediately if no @Configuration classes were found
	if (configCandidates.isEmpty()) {
		return;
	}

	// Sort by previously determined @Order value, if applicable
	configCandidates.sort((bd1, bd2) -> {
		int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
		int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
		return Integer.compare(i1, i2);
	});

	// Detect any custom bean name generation strategy supplied through the enclosing application context
    //检测上下文提供的任何自定义bean名称生成策略
	SingletonBeanRegistry sbr = null;
	if (registry instanceof SingletonBeanRegistry) {
		sbr = (SingletonBeanRegistry) registry;
		if (!this.localBeanNameGeneratorSet) {
			BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(
					AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
			if (generator != null) {
				this.componentScanBeanNameGenerator = generator;
				this.importBeanNameGenerator = generator;
			}
		}
	}

	if (this.environment == null) {
		this.environment = new StandardEnvironment();
	}

	// Parse each @Configuration class
    //ConfigurationClassParser解析所有配置类集合
	ConfigurationClassParser parser = new ConfigurationClassParser(
			this.metadataReaderFactory, this.problemReporter, this.environment,
			this.resourceLoader, this.componentScanBeanNameGenerator, registry);

	Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
	Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
	do {
		parser.parse(candidates);
		parser.validate();

		Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
		configClasses.removeAll(alreadyParsed);

		// Read the model and create bean definitions based on its content
		if (this.reader == null) {
			this.reader = new ConfigurationClassBeanDefinitionReader(
					registry, this.sourceExtractor, this.resourceLoader, this.environment,
					this.importBeanNameGenerator, parser.getImportRegistry());
		}
        //ConfigurationClassBeanDefinitionReader 注册配置类中找到的BeanDefinition
		this.reader.loadBeanDefinitions(configClasses);
		alreadyParsed.addAll(configClasses);

		candidates.clear();
		if (registry.getBeanDefinitionCount() > candidateNames.length) {
			String[] newCandidateNames = registry.getBeanDefinitionNames();
			Set<String> oldCandidateNames = new HashSet<>(Arrays.asList(candidateNames));
			Set<String> alreadyParsedClasses = new HashSet<>();
			for (ConfigurationClass configurationClass : alreadyParsed) {
				alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
			}
			for (String candidateName : newCandidateNames) {
				if (!oldCandidateNames.contains(candidateName)) {
					BeanDefinition bd = registry.getBeanDefinition(candidateName);
					if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd, this.metadataReaderFactory) &&
							!alreadyParsedClasses.contains(bd.getBeanClassName())) {
						candidates.add(new BeanDefinitionHolder(bd, candidateName));
					}
				}
			}
			candidateNames = newCandidateNames;
		}
	}
	while (!candidates.isEmpty());

	// Register the ImportRegistry as a bean in order to support ImportAware @Configuration classes
	if (sbr != null && !sbr.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
		sbr.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry());
	}

	if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory) {
		// Clear cache in externally provided MetadataReaderFactory; this is a no-op
		// for a shared cache since it'll be cleared by the ApplicationContext.
		((CachingMetadataReaderFactory) this.metadataReaderFactory).clearCache();
	}
}
```

- 通过BeanDefinitionRegistry查找当前Spring容器中所有BeanDefinition
- 通过ConfigurationClassUtils.checkConfigurationClassCandidate() 检查BeanDefinition是否为 “完全配置类” ，并对配置类做标记，放入集合待后续处理
- 通过 ConfigurationClassParser解析器 parse解析配置类集合，尝试通过它们找到其它配置类
- 使用 ConfigurationClassBeanDefinitionReader 注册通过所发现的配置类中找到的所有beanDefinition
- 还未被处理过的 “完全配置类” 重复上面两项操作

##### ConfigurationClassParser

​	ConfigurationClassParser#parse() 解析构建配置类，主要逻辑在doProcessConfigurationClass()方法中，会处理成员内部类、@PropertySource、@ComponentScan、@Import、@ImportSource、@Bean方法等：

1. 处理配置类的成员内部类： 检查其是否为“完全/简化配置类”，是则对其继续分析处理并将其放入分析器的属性configurationClasses
2. 处理@PropertySource： 将找到的PropertySource添加到environment的PropertySource集合
3. 处理@ComponentScan： 扫描到的@Component类BeanDefinition就直接注册到Spring容器；如果组件为配置类，继续分析处理并将其放入分析器的属性configurationClasses
4. 处理@Import
   1. 处理ImportSelector： 如果是DeferredImportSelector，如SpringBoot的自动配置导入，添加到deferredImportSelectors，延迟进行processImports()；其它通过ImportSelector找到的类，继续调用processImports()，要么是@Configuration配置类继续解析，要么是普通组件导入Spring容器
   2. 处理ImportBeanDefinitionRegistrar： 调用当前配置类的addImportBeanDefinitionRegistrar()，后面委托它注册其它bean定义
   3. 其它Import： 调用processConfigurationClass()继续解析，最终要么是配置类放入configurationClasses，要么是普通组件导入Spring容器
5. 处理@ImportResource： 添加到配置类的importedResources集合，后续ConfigurationClassBeanDefinitionReader#loadBeanDefinitions()时再使用这些导入的BeanDefinitionReader读取Resource中的bean定义并注册
6. 处理@Bean： 获取所有@Bean方法，并添加到配置类的beanMethods集合
7. 处理配置类接口上的default methods
8. 检查是否有未处理的父类： 如果配置类有父类，且其不在解析器维护的knownSuperclasses中，对其调用doProcessConfigurationClass()重复如上检查，直到不再有父类或父类在knownSuperclasses中已存在

```java
protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
			throws IOException {
    
	//处理配置类的成员内部类
	if (configClass.getMetadata().isAnnotated(Component.class.getName())) {
		// Recursively process any member (nested) classes first
		processMemberClasses(configClass, sourceClass);
	}

	// Process any @PropertySource annotations
    //处理@PropertySource
	for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
			sourceClass.getMetadata(), PropertySources.class,
			org.springframework.context.annotation.PropertySource.class)) {
		if (this.environment instanceof ConfigurableEnvironment) {
			processPropertySource(propertySource);
		}
		else {
			logger.info("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
					"]. Reason: Environment must implement ConfigurableEnvironment");
		}
	}

	// Process any @ComponentScan annotations
    //处理@ComponentScan
	Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
			sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
	if (!componentScans.isEmpty() &&
			!this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
		for (AnnotationAttributes componentScan : componentScans) {
			// The config class is annotated with @ComponentScan -> perform the scan immediately
			Set<BeanDefinitionHolder> scannedBeanDefinitions =
					this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
			// Check the set of scanned definitions for any further config classes and parse recursively if needed
			for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
				BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
				if (bdCand == null) {
					bdCand = holder.getBeanDefinition();
				}
				if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
					parse(bdCand.getBeanClassName(), holder.getBeanName());
				}
			}
		}
	}

	// Process any @Import annotations
    //处理@Import
	processImports(configClass, sourceClass, getImports(sourceClass), true);

	// Process any @ImportResource annotations
    //处理@ImportResource
	AnnotationAttributes importResource =
			AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
	if (importResource != null) {
		String[] resources = importResource.getStringArray("locations");
		Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
		for (String resource : resources) {
			String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
			configClass.addImportedResource(resolvedResource, readerClass);
		}
	}

	// Process individual @Bean methods
    //处理@Bean
	Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
	for (MethodMetadata methodMetadata : beanMethods) {
		configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
	}

	// Process default methods on interfaces
    //处理配置类接口上的default methods
	processInterfaces(configClass, sourceClass);

	// Process superclass, if any
	if (sourceClass.getMetadata().hasSuperClass()) {
		String superclass = sourceClass.getMetadata().getSuperClassName();
		if (superclass != null && !superclass.startsWith("java") &&
				!this.knownSuperclasses.containsKey(superclass)) {
			this.knownSuperclasses.put(superclass, configClass);
			// Superclass found, return its annotation metadata and recurse
			return sourceClass.getSuperClass();
		}
	}

	// No superclass -> processing is complete
	return null;
}
```

##### ConfigurationClassBeanDefinitionReader

loadBeanDefinitions()继续读取已经构建好的ConfigurationClass配置类中的成员变量，注册beanDefinition

1. 根据ConfigurationPhase.REGISTER_BEAN阶段条件判断配置类是否需要跳过
2. 如果configClass.isImported()，将配置类自身注册为beanDefinition
3. 注册配置类所有@Bean方法为beanDefinition
4. 注册由@ImportedResources来的beanDefinition，即通过其它类型Resource的BeanDefinitionReader读取BeanDefinition并注册，如xml格式的配置源 XmlBeanDefinitionReader
5. 注册由ImportBeanDefinitionRegistrars来的beanDefinition

```java
private void loadBeanDefinitionsForConfigurationClass(
			ConfigurationClass configClass, TrackedConditionEvaluator trackedConditionEvaluator) {

    /**
	 * 根据ConfigurationPhase.REGISTER_BEAN阶段条件判断配置类是否需要跳过
	 * 循环判断配置类以及导入配置类的类，使用ConfigurationPhase.REGISTER_BEAN	   * 阶段条件判断是否需要跳过，只要配置类或导入配置类的类需要跳过即返回跳过
	 */
	if (trackedConditionEvaluator.shouldSkip(configClass)) {
		String beanName = configClass.getBeanName();
		if (StringUtils.hasLength(beanName) && this.registry.containsBeanDefinition(beanName)) {
			this.registry.removeBeanDefinition(beanName);
		}
		this.importRegistry.removeImportingClass(configClass.getMetadata().getClassName());
		return;
	}
	// 1、如果当前配置类是通过内部类导入 或 @Import导入，将配置类自身注册为		beanDefinition
	if (configClass.isImported()) {
		registerBeanDefinitionForImportedConfigurationClass(configClass);
	}
    // 2、注册配置类所有@Bean方法为beanDefinition
	for (BeanMethod beanMethod : configClass.getBeanMethods()) {
		loadBeanDefinitionsForBeanMethod(beanMethod);
	}
	// 3、注册由@ImportedResources来的beanDefinition
	// 即通过其它类型Resource的BeanDefinitionReader读取BeanDefinition并注册
	loadBeanDefinitionsFromImportedResources(configClass.getImportedResources());
	// 4、注册由ImportBeanDefinitionRegistrars来的beanDefinition
    loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars());
}
```

