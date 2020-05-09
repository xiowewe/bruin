#### BeanPostProcessor相关接口

##### BeanPostProcessor

接口方法：

```java
//return null 时后续后处理器将不执行，会出现nullPointException,默认return bean就行
default Object postProcessBeforeInitialization(Object bean, String beanName) ;
default Object postProcessAfterInitialization(Object bean, String beanName);
```

BeanPostProcessor的两个接口分别在bean初始化方法调用前后分别执行

```java
protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
    	//权限限制
		if (System.getSecurityManager() != null) {
			AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
				invokeAwareMethods(beanName, bean);
				return null;
			}, getAccessControlContext());
		}
		else {
             //调用所欲Aware接口
			invokeAwareMethods(beanName, bean);
		}

		Object wrappedBean = bean;
		if (mbd == null || !mbd.isSynthetic()) {
            //bean 后处理器的postProcessBeforeInitialization方法
			wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
		}

		try {
             //调用bean 的初始化方法
			invokeInitMethods(beanName, wrappedBean, mbd);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					(mbd != null ? mbd.getResourceDescription() : null),
					beanName, "Invocation of init method failed", ex);
		}
		if (mbd == null || !mbd.isSynthetic()) {
            //bean 后处理器的postProcessAfterInitialization方法
			wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
		}

		return wrappedBean;
	}
```



##### InstantiationAwareBeanPostProcessor

继承BeanPostProcessor，接口方法：

```java
//resolveBeforeInstantiation() 给BeanPostProcessor一个返回代理而不是目标bean实例的机会
//如果返回非null对象（即返回代理），那接下来除了postProcessAfterInitialization方法会被执行以外，其它bean构造的那些方法都不再执行。反之都将执行
Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName)；
    
// 返回值是boolean类型，如果返回true，目标实例内部的返回值会被populate，否则populate这个过程会被忽视
boolean postProcessAfterInstantiation(Object bean, String beanName)；

// 返回值是PropertyValues，可以使用一个全新的PropertyValues代替原先的PropertyValues用来覆盖属性设置或者直接在参数pvs上修改。如果返回值是null，那么会忽略属性设置这个过程(所有属性不论使用什么注解，最后都是null)
PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)；
    
//另外还有继承自BeanPostProcessor的接口方法
```

1. postProcessBeforeInstantiation 调用在bean的实例化之前调用：resolveBeforeInstantiation（给BeanPostProcessor一个返回代理而不是目标bean实例的机会）

   ```java
   protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
       Object bean = null;
       if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
           if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
               Class<?> targetType = determineTargetType(beanName, mbd);
               if (targetType != null) {
                   //调用postProcessBeforeInstantiation 方法
                   bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                   //返回非null对象，接下来除了postProcessAfterInitialization方法之后直接返回bean，后续doCreateBean方法不执行
                   if (bean != null) {
                       bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                   }
               }
           }
           mbd.beforeInstantiationResolved = (bean != null);
       }
       return bean;
   }
   ```

   

2. postProcessAfterInstantiation 调用在bean的实例化之后的populateBean()属性填充方法，如果返回true，目标实例内部的返回值会被populate，否则populate这个过程会被忽视

   ```java
   protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
       // .......
       if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
           for (BeanPostProcessor bp : getBeanPostProcessors()) {
               if (bp instanceof InstantiationAwareBeanPostProcessor) {
                   InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                   //返回true
                   if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                       continueWithPropertyPopulation = false;
                       break;
                   }
               }
           }
       }
       //直接返回
       if (!continueWithPropertyPopulation) {
           return;
       }
       // .......
       //下面是简化代码
       for (BeanPostProcessor bp : getBeanPostProcessors()) {
   		if (bp instanceof InstantiationAwareBeanPostProcessor) {
               //postProcessProperties 执行
               pvsToUse = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
           }
       //填充属性
       applyPropertyValues(beanName, mbd, bw, pvs);
   }
   ```

3. postProcessProperties执行在填充属性applyPropertyValues()之前执行

4. 另外还有继承自BeanPostProcessor的接口方法同样在初始化前后执行

##### SmartInstantiationAwareBeanPostProcessor

继承InstantiationAwareBeanPostProcessor，接口

```java
// 预测Bean的类型，返回第一个预测成功的Class类型，如果不能预测返回null
Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException;

// 选择合适的构造器，比如目标对象有多个构造器，在这里可以进行一些定制化，选择合适的构造器
// beanClass参数表示目标实例的类型，beanName是目标实例在Spring容器中的name
// 返回值是个构造器数组，如果返回null，会执行下一个PostProcessor的determineCandidateConstructors方法；否则选取该PostProcessor选择的构造器
Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException;

// 获得提前暴露的bean引用。主要用于解决循环引用的问题
// 只有单例对象才会调用此方法
Object getEarlyBeanReference(Object bean, String beanName) throws BeansException;

```

1. predictBeanType方法用于预测Bean的类型，返回第一个预测成功的Class类型，如果不能预测返回null。主要在于BeanDefinition无法确定Bean类型的时候调用该方法来确定类型
2. determineCandidateConstructors方法用于选择合适的构造器，比如类有多个构造器，可以实现这个方法选择合适的构造器并用于实例化对象。该方法在postProcessBeforeInstantiation方法和postProcessAfterInstantiation方法之间调用，如果postProcessBeforeInstantiation方法返回了一个新的实例代替了原本该生成的实例，那么该方法会被忽略
3. getEarlyBeanReference主要用于解决循环引用问题

##### DestructionAwareBeanPostProcessor

继承BeanPostProcessor，接口

```java
//bean在Spring在容器中destroy调用
void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;
```

##### MergedBeanDefinitionPostProcessor

继承BeanPostProcessor，接口

```java
//在createBeanInstance和populateBean之间执行，即实例化、属性填充之间
void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);
```

#### Spring内置BeanPostProcessor应用

Spring内置了一些很有用的BeanPostProcessor接口实现类。比如有AutowiredAnnotationBeanPostProcessor、RequiredAnnotationBeanPostProcessor、CommonAnnotationBeanPostProcessor、EventListenerMethodProcessor等。这些Processor会处理各自的场景。正是有了这些processor，把bean的构造过程中的一部分功能分配给了这些processor处理，减轻了BeanFactory的负担。

##### ApplicationContextAwareProcessor

实现BeanPostProcessor接口

```java
public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (!(bean instanceof EnvironmentAware || bean instanceof EmbeddedValueResolverAware ||
          bean instanceof ResourceLoaderAware || bean instanceof ApplicationEventPublisherAware ||
          bean instanceof MessageSourceAware || bean instanceof ApplicationContextAware)){
        return bean;
    }

    AccessControlContext acc = null;

    if (System.getSecurityManager() != null) {
        acc = this.applicationContext.getBeanFactory().getAccessControlContext();
    }

    if (acc != null) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            invokeAwareInterfaces(bean);
            return null;
        }, acc);
    }
    else {
        //为实现*Aware接口的bean调用该Aware接口定义的方法，并传入对应的参数。比如实现EnvironmentAware接口的bean在该Processor内部会调用EnvironmentAware接口的setEnvironment方法，并把Spring容器内部的ConfigurableEnvironment传递进去。
        invokeAwareInterfaces(bean);
    }

    return bean;
}
```

1. Spring容器的refresh方法内部调用prepareBeanFactory方法，prepareBeanFactory方法会添加ApplicationContextAwareProcessor到BeanFactory中
2. ApplicationContextAwareProcessor为实现*Aware接口的bean调用该Aware接口定义的方法，并传入对应的参数。比如实现EnvironmentAware接口的bean在该Processor内部会调用EnvironmentAware接口的setEnvironment方法，并把Spring容器内部的ConfigurableEnvironment传递进去

##### CommonAnnotationBeanPostProcessor

继承InitDestroyAnnotationBeanPostProcessor和实现InstantiationAwareBeanPostProcessor

- CommonAnnotationBeanPostProcessor 的注入

  AnnotationConfigApplicationContext 注解bean容器在创建或者ContextNamespaceHandler 在处理context:annotation-config 属性时会调用AnnotationConfigUtils类的registerAnnotationConfigProcessors方法，CommonAnnotationBeanPostProcessor 也正是通过该方法被封装成RootBeanDefinition并注册到Spring容器中（其中还包括熟知的ConfigurationAnnotationProcessor、AutowiredAnnotationProcessor等6个6个处理器）

- CommonAnnotationBeanPostProcessor 的作用

  要处理@Resource、@PostConstruct和@PreDestroy注解的实现。

  - postProcessProperties 处理@Resource

    ```java
    public PropertyValues postProcessProperties(
        PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
      // 找出bean中被@Resource注解修饰的属性(Field)和方法(Method)
      InjectionMetadata metadata = findResourceMetadata(beanName, bean.getClass(), pvs);
      try {
        // 注入到bean中
        metadata.inject(bean, beanName, pvs);
      }
      catch (Throwable ex) {
        throw new BeanCreationException(beanName, "Injection of resource dependencies failed", ex);
      }
      return pvs;
    }
    ```

    

  - @PostConstruct和@PreDestroy注解

    其父类InitDestroyAnnotationBeanPostProcessor的postProcessMergedBeanDefinition接口会找出被@PostConstruct和@PreDestroy注解修饰的方法，然后通过postProcessBeforeInitialization接口处理@PostConstruct 以及 postProcessBeforeDestruction处理@PreDestroy注解的方法

    ```java
    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
      if (beanType != null) {
        // 找出被@PostConstruct和@PreDestroy注解修饰的方法
        LifecycleMetadata metadata = findLifecycleMetadata(beanType);
        metadata.checkConfigMembers(beanDefinition);
      }
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
      LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
      try {
        // postProcessBeforeInitialization在实例初始化之前调用
        // 这里调用了被@PostConstruct注解修饰的方法
        metadata.invokeInitMethods(bean, beanName);
      }
      catch (InvocationTargetException ex) {
        throw new BeanCreationException(beanName, "Invocation of init method failed", ex.getTargetException());
      }
      catch (Throwable ex) {
        throw new BeanCreationException(beanName, "Couldn't invoke init method", ex);
      }
      return bean;
    }
    
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
      LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
      try {
        // postProcessBeforeDestruction在实例销毁之前调用
        // 这里调用了被@PreDestroy注解修饰的方法
        metadata.invokeDestroyMethods(bean, beanName);
      }
      catch (InvocationTargetException ex) {
        String msg = "Invocation of destroy method failed on bean with name '" + beanName + "'";
        if (logger.isDebugEnabled()) {
          logger.warn(msg, ex.getTargetException());
        }
        else {
          logger.warn(msg + ": " + ex.getTargetException());
        }
      }
      catch (Throwable ex) {
        logger.error("Couldn't invoke destroy method on bean with name '" + beanName + "'", ex);
      }
    }
    ```

    

##### AutowiredAnnotationBeanPostProcessor

继承InstantiationAwareBeanPostProcessorAdapter和实现MergedBeanDefinitionPostProcessor

- AutowiredAnnotationBeanPostProcessor的注入

  同样是在AnnotationConfigUtils类的registerAnnotationConfigProcessors方法被注册到Spring容器中

- AutowiredAnnotationBeanPostProcessor 作用

  主要处理@Autowired、@Value、@Lookup和@Inject注解的实现，实现同CommonAnnotationBeanPostProcessor

  ```java
  @Override
  public PropertyValues postProcessPropertyValues(
      PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
    // 找出被@Autowired、@Value以及@Inject注解修饰的属性和方法
    InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
    try {
      // 注入到bean中
      metadata.inject(bean, beanName, pvs);
    }
    catch (Throwable ex) {
      throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
    }
    return pvs;
  }
  ```

  @Autowired注解可以在构造器中使用，所以AutowiredAnnotationBeanPostProcessor实现了determineCandidateConstructors方法：

  ```java
  @Override
  public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, final String beanName) throws BeansException {
    ...
    for (Constructor<?> candidate : rawCandidates) { // 遍历所有的构造器
      // 找出被@Autowired注解修饰的构造器
      AnnotationAttributes ann = findAutowiredAnnotation(candidate);
      if (ann != null) {
        ...
        candidates.add(candidate);
      }
      else if (candidate.getParameterTypes().length == 0) {
        defaultConstructor = candidate;
      }
    }
    if (!candidates.isEmpty()) { // 有找到的话使用这些构造器
      ...
      candidateConstructors = candidates.toArray(new Constructor<?>[candidates.size()]);
    }
    else { // 否则使用默认的构造器
      candidateConstructors = new Constructor<?>[0];
    }
    ...
  }
  ```

  

##### AbstractAutoProxyCreator

实现SmartInstantiationAwareBeanPostProcessor

```java
ublic Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
  // 生成缓存key
  // AbstractAutoProxyCreator内部有个Map用于存储代理类的缓存信息
  Object cacheKey = getCacheKey(beanClass, beanName);
  // targetSourcedBeans是个String集合，如果这个bean被内部的TargetSourceCreator数组属性处理过，那么targetSourcedBeans就会存储这个bean的beanName
  // 如果targetSourcedBeans内部没有包括当前beanName
  if (beanName == null || !this.targetSourcedBeans.contains(beanName)) {
    // advisedBeans属性是个Map<Object, Boolean>类型的map，key为cacheKey，value是个Boolean，如果是true，说明这个bean已经被wrap成代理类，否则还是原先的bean
    // 这里判断cacheKey是否已经被wrap成代理类，如果没有，返回null，走Spring默认的构造bean流程
    if (this.advisedBeans.containsKey(cacheKey)) {
      return null;
    }
    // isInfrastructureClass方法判断该bean是否是aop相关的bean，比如Advice、Advisor、AopInfrastructureBean
    // shouldSkip方法默认返回false，子类可覆盖。比如AspectJAwareAdvisorAutoProxyCreator子类进行了覆盖，它内部会找出Spring容器中Advisor类型的bean，然后进行遍历判断处理的bean是否是这个Advisor，如果是则过滤
    if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
      this.advisedBeans.put(cacheKey, Boolean.FALSE);
      return null;
    }
  }

  if (beanName != null) {
    // 遍历内部的TargetSourceCreator数组属性，根据bean信息得到TargetSource
    // 默认情况下TargetSourceCreator数组属性是空的
    TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
    if (targetSource != null) {
      // 添加beanName到targetSourcedBeans中，证明这个bean被自定义的TargetSourceCreator处理过
      this.targetSourcedBeans.add(beanName);
      // 得到Advice
      Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
      // 创建代理
      Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
      // 添加到proxyTypes属性中
      this.proxyTypes.put(cacheKey, proxy.getClass());
      // 返回这个代理类，这样后续对该bean便不再处理，除了postProcessAfterInitialization过程
      return proxy;
    }
  }

  return null;
}
```

从这个postProcessBeforeInstantiation方法我们得出：如果使用了自定义的TargetSourceCreator，并且这个TargetSourceCreator得到了处理bean的TargetSource结果，那么直接基于这个bean和TargetSource结果构造出代理类。这个过程发生在postProcessBeforeInstantiation方法中，所以这个代理类直接代替了原本该生成的bean。如果没有使用自定义的TargetSourceCreator，那么走默认构造bean的流程。

```java
@Override
public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
  // 生成缓存key
  Object cacheKey = getCacheKey(bean.getClass(), beanName);
  // earlyProxyReferences用来存储提前暴露的代理对象的缓存key，这里判断是否已经处理过，没处理过的话放到earlyProxyReferences里
  if (!this.earlyProxyReferences.contains(cacheKey)) {
    this.earlyProxyReferences.add(cacheKey);
  }
  return wrapIfNecessary(bean, beanName, cacheKey);
}

protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
  // 如果已经使用了自定义的TargetSourceCreator生成了代理类，直接返回这个代理类
  if (beanName != null && this.targetSourcedBeans.contains(beanName)) {
    return bean;
  }
  // 该bean已经没有被wrap成代理类，直接返回原本生成的实例
  if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
    return bean;
  }
  // 如果是处理aop自身相关的bean或者这些bean需要被skip，也直接返回这些bean
  if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
    this.advisedBeans.put(cacheKey, Boolean.FALSE);
    return bean;
  }

  // 得到Advice
  Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
  if (specificInterceptors != DO_NOT_PROXY) { // 如果被aop处理了
    // 添加到advisedBeans属性中，说明该bean已经被wrap成代理类
    this.advisedBeans.put(cacheKey, Boolean.TRUE);
    // 创建代理类
    Object proxy = createProxy(
        bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
    // 添加到proxyTypes属性中
    this.proxyTypes.put(cacheKey, proxy.getClass());
    return proxy;
  }
  // 如果没有被aop处理，添加到advisedBeans属性中，并说明不是代理类
  this.advisedBeans.put(cacheKey, Boolean.FALSE);
  return bean;
}

@Override
public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
  if (bean != null) {
    Object cacheKey = getCacheKey(bean.getClass(), beanName);
    if (!this.earlyProxyReferences.contains(cacheKey)) {
      return wrapIfNecessary(bean, beanName, cacheKey);
    }
  }
  return bean;
}
```

从上面这些方法看出，要实例化的bean会通过wrapIfNecessary进行处理，wrapIfNecessary方法会根据情况是否wrap成代理类，最终返回这个结果。getEarlyBeanReference和postProcessAfterInitialization处理流程是一样的，唯一的区别是getEarlyBeanReference是针对单例的，而postProcessAfterInitialization方法是针对prototype的，针对prototype的话，每次实例化都会wrap成代理对象，而单例的话只需要wrap一次即可。

AbstractAutoProxyCreator抽象类有基于注解的子类AnnotationAwareAspectJAutoProxyCreator。这个AnnotationAwareAspectJAutoProxyCreator会扫描出Spring容器中带有@Aspect注解的bean，然后在getAdvicesAndAdvisorsForBean方法中会根据这个aspect查看是否被拦截，如果被拦截那么就wrap成代理类。

默认情况下，AbstractAutoProxyCreator相关的BeanPostProcessor是不会注册到Spring容器中的。比如在SpringBoot中加入aop-starter之后，会触发AopAutoConfiguration自动化配置，然后将AnnotationAwareAspectJAutoProxyCreator注册到Spring容器中。

#### BeanPostProcessor 外部应用





参考：https://fangjian0423.github.io/2017/06/20/spring-bean-post-processor/

https://fangjian0423.github.io/2017/06/24/spring-embedded-bean-post-processor/