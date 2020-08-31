#### BeanFactoryPostProcessor

​	 容器允许BeanFactoryPostProcessor在容器实例化任何Bean之前读取配置元数据，并有可能修改它。也可以配置多个BeanFactoryPostProcessor，并通过实现Ordered/PriorityOrdered接口设置"order"属性来控制BeanFactoryPostProcessor的执行顺序。BeanFactoryPostProcessor的作用范围是容器级别，不会对定义之外容器里的Bean作后置处理。区别于BeanPostProcessor，如果想改变实际的Bean实例，应该使用BeanPostProcessor。

##### 激活注册BeanFactoryPostProcessor

   BeanFactoryPostProcessor的激活实在AbstractApplicationContext#refresh() 中invokeBeanFactoryPostProcessors(beanFactory); 具体的激活逻辑交给PostProcessorRegistrationDelegate委托类：

1. BeanFactoryPostProcessor的处理分为BeanDefinitionRegistryPostProcessor类型的特殊处理和普通BeanFactoryPostProcessor的处理，同时两中都包括硬编码、配置注入两种型式的后处理器
2. 硬编码的形式通过AbstractApplicationContext#addBeanFactoryPostProcessor 添加，存放在beanFactoryPostProcessors中
3. BeanDefinitionRegistryPostProcessor类型的后处理器相比普通类型后处理器需要先执行postProcessBeanDefinitionRegistry接口方法

```java
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {//beanFactoryPostProcessors 存放硬编码添加的后处理器

	// Invoke BeanDefinitionRegistryPostProcessors first, if any.
	Set<String> processedBeans = new HashSet<>();

	if (beanFactory instanceof BeanDefinitionRegistry) {
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
         //硬编码 普通类型的后处理器
		List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
         //硬编码 BeanDefinitionRegistryPostProcessor类型的后处理器
		List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

		for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
             //1、【硬编码】的BeanDefinitionRegistryPostProcessor类型的后处理器先执行
             //postProcessBeanDefinitionRegistry接口方法
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
		List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

		// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
         //2、激活处理 【配置方式】 的BeanDefinitionRegistryPostProcessors类型的后处理器的					    //postProcessBeanDefinitionRegistry接口方法，按照PriorityOrdered、Ordered、none 的顺序
        // 2.1 实现PriorityOrdered
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
				processedBeans.add(ppName);
			}
		}
		sortPostProcessors(currentRegistryProcessors, beanFactory);
		registryProcessors.addAll(currentRegistryProcessors);
		invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
		currentRegistryProcessors.clear();

		// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
         //2.2 实现Ordered
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
         //2.3 none实现
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
         //3、激活处理【硬编码】BeanDefinitionRegistryPostProcessors类型postProcessBeanFactory接口
		invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
         //4、激活处理【硬编码】普通类型postProcessBeanFactory接口
		invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
	}

	else {
		// Invoke factory processors registered with the context instance.
		invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
	}

	// Do not initialize FactoryBeans here: We need to leave all regular beans
	// uninitialized to let the bean factory post-processors apply to them!
	String[] postProcessorNames =
			beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

	// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
	// Ordered, and the rest.
    //5、激活处理 【配置方式】 的BeanDefinitionRegistryPostProcessors类型的和普通类型后处理器的				//postProcessBeanFactory接口方法，按照PriorityOrdered、Ordered、none 的顺序
    // 5.1 实现PriorityOrdered
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
    // 5.2 实现PriorityOrdered
	List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
	for (String postProcessorName : orderedPostProcessorNames) {
		orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
	}
	sortPostProcessors(orderedPostProcessors, beanFactory);
	invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

	// Finally, invoke all other BeanFactoryPostProcessors.
    // 5.3 实现PriorityOrdered
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

执行顺序（好晕）：

1. 【硬编码】 BeanDefinitionRegistryPostProcessor类型	postProcessBeanDefinitionRegistry接口
2. 【配置方式】 BeanDefinitionRegistryPostProcessor类型  postProcessBeanDefinitionRegistry接口
3. 【硬编码】 BeanDefinitionRegistryPostProcessor类型	postProcessBeanFactory接口
4. 【硬编码】 普通类型	postProcessBeanFactory接口
5. 【配置方式】 普通类型和BeanDefinitionRegistryPostProcessor类型	postProcessBeanFactory接口

##### 自定义BeanFactoryPostProcessor

```java
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for(String beanName : beanNames){
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(new StringValueResolver() {
                @Override
                public String resolveStringValue(String s) {
                    if(ObjectUtils.nullSafeEquals(s, "admin")){
                        return "****";
                    }
                    return s;
                }
            });
            visitor.visitBeanDefinition(bd);
        }
    }
}
```

```java
@Import(AnnotationBeanDefinitionDemo.Config.class)
public class AnnotationBeanDefinitionDemo {

    public static void main(String[] args) {
        // 创建 BeanFactory 容器
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        // 注册 Configuration Class（配置类）
        applicationContext.register(AnnotationBeanDefinitionDemo.class);
        
        applicationContext.addBeanFactoryPostProcessor(new MyBeanFactoryPostProcessor());

        // 启动 Spring 应用上下文
        applicationContext.refresh();
        // 按照类型依赖查找
        System.out.println(applicationContext.getBean("userBean"));
        // 显示地关闭 Spring 应用上下文
        applicationContext.close();
    }



    @Component
    public static class Config {

        @Bean(name = "userBean")
        public UserBean userBean() {
            UserBean userBean = new UserBean();
            userBean.setName("admin");
            userBean.setAge(100);
            userBean.setPassword("admin");
            return userBean;
        }
    }

    
}
```



##### BeanFactoryPostProcessor的应用