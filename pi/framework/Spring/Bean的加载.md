#### BeanDefinition

BeanDefinition描述了一个bean实例，该实例具有属性值、构造函数参数值，具体实现提供的更多信息。BeanDefinition仅仅是一个最简单的接口，主要功能是允许BeanFactoryPostProcessor 例如PropertyPlaceHolderConfigure 能够检索并修改属性值和别的bean的元数据。

##### BeanDefinition类结构

- BeanDefinition继承`AttributeAccessor`、 `BeanMetadataElement`
  - `AttributeAccessor`定义了最基本的对任意对象的元数据的修改或者获取
  - `BeanMetadataElement`接口提供了一个getResource()方法,用来传输一个可配置的源对象
- 子接口AnnotationBeanDefinition
- BeanDefinition的抽象类AbstractBeanDefinition
  - 子类RootBeanDefinition
  - 子类ChildBeanDefinition
  - 子类GenericBeanDefinition

##### AbstractBeanDefinition

定义的属性基本囊括了Bean实例化需要的所有信息，大概包括：

属性包括：

1. Bean的描述信息（例如是否是抽象类、是否单例）
2. depends-on属性（String类型，不是Class类型）
3. 自动装配的相关信息
4. init函数、destroy函数的名字（String类型）
5. 工厂方法名、工厂类名（String类型，不是Class类型）
6. 构造函数形参的值
7. 被IOC容器覆盖的方法
8. Bean的属性以及对应的值（在初始化后会进行填充）

##### ChildBeanDefinition

ChildBeanDefinition从父类继承构造参数值，属性值并可以重写父类的方法，同时也可以增加新的属性或者方法：如指定初始化方法，销毁方法或者静态工厂方法。depends on，autowire mode，dependency check，sigleton，lazy init 一般由子类自行设定。

**注意**：从spring 2.5 开始，提供了一个更好的注册bean definition类GenericBeanDefinition，它支持动态定义父依赖，方法是GenericBeanDefinition.setParentName(java.lang.String)，GenericBeanDefinition可以有效的替代ChildBeanDefinition的绝大分部使用场合。

##### GenericBeanDefinition

是一站式的标准bean definition，除了具有指定类、可选的构造参数值和属性参数这些其它bean definition一样的特性外，它还具有通过parenetName属性来灵活设置parent bean definition。

##### RootBeanDefinition

RootBeanDefinition定义表明它是一个可合并的bean definition：即在spring beanFactory运行期间，可以返回一个特定的bean。RootBeanDefinition可以作为一个重要的通用的bean definition 视图。RootBeanDefinition用来在配置阶段进行注册bean definition。涉及到的类：BeanDefinitionHolder，根据名称或者别名持有beanDefinition。可以为一个内部bean 注册为placeholder。从spring 2.5后，编写注册bean definition有了更好的的方法：GenericBeanDefinition。GenericBeanDefinition支持动态定义父类依赖，而非硬编码作为root bean definition。

RootBeanDefinition大致包括：

1. 定义了id、别名与Bean的对应关系（BeanDefinitionHolder）
2. Bean的注解（AnnotatedElement）
3. 具体的工厂方法（Class类型），包括工厂方法的返回类型，工厂方法的Method对象
4. 构造函数、构造函数形参类型
5. Bean的class对象



#### Bean的加载

1. 转换对应的beanName
2. 尝试从缓存中加载单例
3. 从bean的实例中获取对象
4. 原型模式的依赖检查
5. 检测parentBeanFactory
6. GenericBeanDefinition转换为RootBeanDefinition
7. 寻找依赖
8. 针对不同的scope进行bean的创建
9. requireType ！= null 类型转换

```java
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
			@Nullable final Object[] args, boolean typeCheckOnly) throws BeansException{

   //提取对应的beanName
   final String beanName = transformedBeanName(name);
   Object bean;
   //尝试从缓存中加载单例
   Object sharedInstance = getSingleton(beanName);
   if (sharedInstance != null && args == null) {
    	//...
 			//从bean的实例中获取对象
      bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
   }

   else {
      // Fail if we're already creating this bean instance:We're assumably within a circular reference.
      if (isPrototypeCurrentlyInCreation(beanName)) {
         throw new BeanCurrentlyInCreationException(beanName);
      }

      // Check if bean definition exists in this factory.
      BeanFactory parentBeanFactory = getParentBeanFactory();
      if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
         // Not found -> check parent.
         String nameToLookup = originalBeanName(name);
         if (parentBeanFactory instanceof AbstractBeanFactory) {
            return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
                  nameToLookup, requiredType, args, typeCheckOnly);
         }
         else if (args != null) {
            // Delegation to parent with explicit args.
            return (T) parentBeanFactory.getBean(nameToLookup, args);
         }
         else if (requiredType != null) {
            // No args -> delegate to standard getBean method.
            return parentBeanFactory.getBean(nameToLookup, requiredType);
         }
         else {
            return (T) parentBeanFactory.getBean(nameToLookup);
         }
      }
      //如果不仅仅是类型检车，需要记录
      if (!typeCheckOnly) {
         markBeanAsCreated(beanName);
      }

      try {
         //GenericBeanDefinition转换为RootBeanDefinition
         final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
         checkMergedBeanDefinition(mbd, beanName, args);

         // 若存在依赖则需要递归实例化依赖的bean
         String[] dependsOn = mbd.getDependsOn();
         if (dependsOn != null) {
            for (String dep : dependsOn) {
               if (isDependent(beanName, dep)) {
                  throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
               }
               registerDependentBean(dep, beanName);
               try {
                  getBean(dep);
               }
               catch (NoSuchBeanDefinitionException ex) {
                  throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
               }
            }
         }

         // 实例化依赖的bean后便可以实例化mbd本身：singleton模式
         if (mbd.isSingleton()) {
            sharedInstance = getSingleton(beanName, () -> {
               try {
                  return createBean(beanName, mbd, args);
               }
               catch (BeansException ex) {
                  // Explicitly remove instance from singleton cache: It might have been put there
                  // eagerly by the creation process, to allow for circular reference resolution.
                  // Also remove any beans that received a temporary reference to the bean.
                  destroySingleton(beanName);
                  throw ex;
               }
            });
            bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
         }
					//prototype模式
         else if (mbd.isPrototype()) {
            // It's a prototype -> create a new instance.
            Object prototypeInstance = null;
            try {
               beforePrototypeCreation(beanName);
               prototypeInstance = createBean(beanName, mbd, args);
            }
            finally {
               afterPrototypeCreation(beanName);
            }
            bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
         }

         else {
            String scopeName = mbd.getScope();
            final Scope scope = this.scopes.get(scopeName);
            if (scope == null) {
               throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
            }
            try {
               Object scopedInstance = scope.get(beanName, () -> {
                  beforePrototypeCreation(beanName);
                  try {
                     return createBean(beanName, mbd, args);
                  }
                  finally {
                     afterPrototypeCreation(beanName);
                  }
               });
               bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
            }
            catch (IllegalStateException ex) {
               throw new BeanCreationException(beanName,
                     "Scope '" + scopeName + "' is not active for the current thread; consider " +
                     "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
                     ex);
            }
         }
      }
      catch (BeansException ex) {
         cleanupAfterBeanCreationFailure(beanName);
         throw ex;
      }
   }

   // 检查类型是否符合bean的实际类型
   if (requiredType != null && !requiredType.isInstance(bean)) {
      try {
         T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
         if (convertedBean == null) {
            throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
         }
         return convertedBean;
      }
      catch (TypeMismatchException ex) {
         if (logger.isTraceEnabled()) {
            logger.trace("Failed to convert bean '" + name + "' to required type '" +
                  ClassUtils.getQualifiedName(requiredType) + "'", ex);
         }
         throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
      }
   }
   return (T) bean;
}
```

##### 转换成对应beanName

- 去除FactoryBean的修饰符：“&”
- 取指定alias所表示的最终beanName

##### 缓存中获取单例bean

创建单例bean的时候会存在依赖注入的情况，而在创建以来的时候为了避免循环以来，在Spring中创建bean的原则是不等bean创建完成就会将创建的ObjectFactory提早曝光加入到缓存中，一旦下一个bean创建时候需要以来上一个bean则直接使用ObjectFactory

```java
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
   //1.尝试从singletonObjects里面获取单例
   Object singletonObject = this.singletonObjects.get(beanName);
   if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
      synchronized (this.singletonObjects) {
         //2.获取不到再从earlySingletonObjects获取
         singletonObject = this.earlySingletonObjects.get(beanName);
         if (singletonObject == null && allowEarlyReference) {
            //3.还获取不到再尝试singletonFactories获取beanName的ObjectFactory
            ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
            if (singletonFactory != null) {
               //4.ObjectFactory#getObject 获取
               singletonObject = singletonFactory.getObject();
               this.earlySingletonObjects.put(beanName, singletonObject);
               this.singletonFactories.remove(beanName);
            }
         }
      }
   }
   return singletonObject;
}
```

earlySingletonObjects：用于保存BeanName和创建bean实例之间的关系，和singletonObjects不同的是：当一个单例bean放到这里后，当bean还在创建过程中，就可以通过getBean获取到，目的是用来检测循环引用

##### 从bean的实例中获取对象

1. `AbstractBeanFactory`#`getObjectForBeanInstance`，对FactoryBean正确行验证和bean转换
2. `FactoryBeanRegistrySupport`#`getObjectFromFactoryBean`
3. `FactoryBeanRegistrySupport`#`doGetObjectFromFactoryBean` 中通过`FactoryBean`#`getObject`获取对象
4. `AbstractAutowireCapableBeanFactory`#`applyBeanPostProcessorAfterInitialization`进行对象BeanPostProcessor初始化的后置处理

##### 检测parentBeanFactory

依赖查找中的层次性查找，类似于双亲委派模式

##### RootBeanDefinition 的转换

GenericBeanDefinition转换为RootBeanDefinition的过程，从XML配置中读取得到的信息存储在GenericBeanDefinition中，但是所有的Bean后续操作都是针对RootBeanDefinition的，所以需要进行转换，转换的同时如果弗雷bean不为空，会一并合父类的属性。`AbstractBeanFactory`#`getMergedBeanDefinition`

##### 获取单例

`getSingleton`重载方法：

1. 检查缓存中是否加载过

2. 记录beanName正在加载的状态

3. 加载单例前记录加载状态

   this.singletonsCurrentlyInCreation.add(beanName)将正在创建的bean加载到缓存中，解决循环以来的问题

4. ObjectFactory参数实例化bean

5. 加载单例后的方法调用

6. 缓存bean并删除各种辅助状态

7. **createBean**返回结果



#### Bean的创建

- `createBean`

  ```java
  protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
        throws BeanCreationException {
    	//省略部分代码。。。。
      RootBeanDefinition mbdToUse = mbd;
  
  		//1.判断需要创建的Bean是否可以实例化，这个类是否可以通过类装载器来载入
  		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
  		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
  			mbdToUse = new RootBeanDefinition(mbd);
  			mbdToUse.setBeanClass(resolvedClass);
  		}
    
    	//2.overrides方法处理
    	mbdToUse.prepareMethodOverrides();
    	
    	//3.是否配置了后置处理器相关处理（如果配置了则返回一个代理）
    	Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
  		if (bean != null) {
  			return bean;
  		}
    
    	//4.创建Bean
   	 	Object beanInstance = doCreateBean(beanName, mbdToUse, args);
  		if (logger.isTraceEnabled()) {
  			logger.trace("Finished creating instance of bean '" + beanName + "'");
  		}
  		return beanInstance;
  }
  ```

- `doCreateBean`

  ```java
  protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args) throws BeanCreationException {
  
     BeanWrapper instanceWrapper = null;
    //1.如果是单例清楚缓存
     if (mbd.isSingleton()) {
        instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
     }
     if (instanceWrapper == null) {
       //2.实例化bean，BeanDefinition转换成BeanWrapper
        instanceWrapper = createBeanInstance(beanName, mbd, args);
     }
     final Object bean = instanceWrapper.getWrappedInstance();
     Class<?> beanType = instanceWrapper.getWrappedClass();
     if (beanType != NullBean.class) {
        mbd.resolvedTargetType = beanType;
     }
  
     // Allow post-processors to modify the merged bean definition.
     synchronized (mbd.postProcessingLock) {
        if (!mbd.postProcessed) {
           try {
             //3.MergedBeanDefinitionPostProcessor应用（Autowired注解正式通过此方法实现诸如类型的预解析）
              applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
           }
           catch (Throwable ex) {
              throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Post-processing of merged bean definition failed", ex);
           }
           mbd.postProcessed = true;
        }
     }
  
     //4.循环依赖处理
     boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
           isSingletonCurrentlyInCreation(beanName));
     if (earlySingletonExposure) {
        if (logger.isTraceEnabled()) {
           logger.trace("Eagerly caching bean '" + beanName +
                 "' to allow for resolving potential circular references");
        }
        addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
     }
  
     // Initialize the bean instance.
     Object exposedObject = bean;
     try {
       //5.属性填充
        populateBean(beanName, mbd, instanceWrapper);
       //6.初始化
        exposedObject = initializeBean(beanName, exposedObject, mbd);
     }
     catch (Throwable ex) {
        if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
           throw (BeanCreationException) ex;
        }
        else {
           throw new BeanCreationException(
                 mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
        }
     }
  	//7.循环依赖检查
     if (earlySingletonExposure) {
        Object earlySingletonReference = getSingleton(beanName, false);
        if (earlySingletonReference != null) {
           if (exposedObject == bean) {
              exposedObject = earlySingletonReference;
           }
           else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
              String[] dependentBeans = getDependentBeans(beanName);
              Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
              for (String dependentBean : dependentBeans) {
                 if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                    actualDependentBeans.add(dependentBean);
                 }
              }
              if (!actualDependentBeans.isEmpty()) {
                 throw new BeanCurrentlyInCreationException(beanName,
                       "Bean with name 。。。。省略");
              }
           }
        }
     }
  
     try {
       //8.注册DisposableBean
        registerDisposableBeanIfNecessary(beanName, bean, mbd);
     }
     catch (BeanDefinitionValidationException ex) {
        throw new BeanCreationException(
              mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
     }
  
     return exposedObject;
  }
  ```

##### 创建Bean实例

`createBeanInstance`，根据Bean使用的策略创建新的实例，如：工厂方法、构造函数自动注入、简单初始化。主要逻辑：

1. 如果在`RootBeanDefinition`中存在factoryMethodName属性（或者配置了factory-method），则使用`instantiateUsingFactoryMethod`根据`RootBeanDefinition`中的配置生成bean的实例
2. `autowireConstructor`解析构造函数并进行实例化。bean可能存在多个构造函数，每个构造函数参数不同，Spring在根据参数和类型判断使用那个构造函数。为了避免重复判断损耗性能，`RootBeanDefinition`中的`resolvedConstructorOrFactoryMethod`缓存了
3. `instantiateBean` 获取实例化策略`InstantiationStrategy`进行实例化

##### Bean的ObjectFactory

```java
//是否提早曝光单例 = bean是否单例 && 是否允许循环依赖 && bean是否在创建中
boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
      isSingletonCurrentlyInCreation(beanName));
if (earlySingletonExposure) {
   if (logger.isTraceEnabled()) {
      logger.trace("Eagerly caching bean '" + beanName +
            "' to allow for resolving potential circular references");
   }
   //获取提前暴露的单例ObjectFactory
   addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
}
```

`getEarlyBeanReference`没有太多逻辑，仅仅是后处理器的调用。解决AB循环依赖。

##### 属性注入

```java
protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
   if (bw == null) {
      if (mbd.hasPropertyValues()) {
         throw new BeanCreationException(
               mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
      }
      else {
         // 没有可填充的属性
         return;
      }
   }

   //给InstantiationAwareBeanPostProcessor最后一次机会再属性设置钱改变Bean
   if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
      for (BeanPostProcessor bp : getBeanPostProcessors()) {
         if (bp instanceof InstantiationAwareBeanPostProcessor) {
            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
            if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
               return;
            }
         }
      }
   }

   PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);

   int resolvedAutowireMode = mbd.getResolvedAutowireMode();
   if (resolvedAutowireMode == AUTOWIRE_BY_NAME || resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
      MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
      //根据名称注入
      if (resolvedAutowireMode == AUTOWIRE_BY_NAME) {
         autowireByName(beanName, mbd, bw, newPvs);
      }
      //根据类型注入
      if (resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
         autowireByType(beanName, mbd, bw, newPvs);
      }
      pvs = newPvs;
   }
	//后处理器已经初始化
   boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
  //需要依赖检查
   boolean needsDepCheck = (mbd.getDependencyCheck() != AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);

   PropertyDescriptor[] filteredPds = null;
   if (hasInstAwareBpps) {
      if (pvs == null) {
         pvs = mbd.getPropertyValues();
      }
      for (BeanPostProcessor bp : getBeanPostProcessors()) {
         if (bp instanceof InstantiationAwareBeanPostProcessor) {
            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
            PropertyValues pvsToUse = ibp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
            if (pvsToUse == null) {
               if (filteredPds == null) {
                  filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
               }
              //对所有需要依赖检查的属性进行后处理
               pvsToUse = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
               if (pvsToUse == null) {
                  return;
               }
            }
            pvs = pvsToUse;
         }
      }
   }
   if (needsDepCheck) {
      if (filteredPds == null) {
         filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
      }
      checkDependencies(beanName, mbd, filteredPds, pvs);
   }
	//属性应用田间到bean中
   if (pvs != null) {
      applyPropertyValues(beanName, mbd, bw, pvs);
   }
}
```

1. InstantiationAwareBeanPostProcessor处理器的postProcessAfterInstantiation函数的应用，可以控制程序是否继续进行属性填充
2. 根据注入类型，提取依赖的bean并统一存入PropertyValues
3. 应用InstantiationAwareBeanPostProcessor处理器的postProcessPropertyValues方法，对属性获取完毕填充前对属性再次处理，典型RequiredAnnotationBeanPostProcessor类对属性的验证
4. 所有PropertyValues中的属性填充BeanWrapper

##### 初始化

```java
protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
   if (System.getSecurityManager() != null) {
      AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
         invokeAwareMethods(beanName, bean);
         return null;
      }, getAccessControlContext());
   }
   else {
     //对特殊的bean处理，Aware、BeanNameAware、BeanClassLoaderAware、BeanFactoryAware
      invokeAwareMethods(beanName, bean);
   }

   Object wrappedBean = bean;
   if (mbd == null || !mbd.isSynthetic()) {
     //初始化前应用后置处理器
      wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
   }

   try {
     //激活InitializingBean、init-method用户自定义初始化方法
      invokeInitMethods(beanName, wrappedBean, mbd);
   }
   catch (Throwable ex) {
      throw new BeanCreationException(
            (mbd != null ? mbd.getResourceDescription() : null),
            beanName, "Invocation of init method failed", ex);
   }
   if (mbd == null || !mbd.isSynthetic()) {
     //初始化后应用后置处理器
      wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
   }

   return wrappedBean;
}
```

1. 激活Aware方法
2. 后置处理器应用BeanPostProcessor
3. 激活InitializingBean、init-method用户自定义初始化方法

##### 注册DisposableBean

```java
protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
   AccessControlContext acc = (System.getSecurityManager() != null ? getAccessControlContext() : null);
   if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
      if (mbd.isSingleton()) {
         //单例模式下需要销毁的bean，此方法会处理实现DisposableBean的bean，并且对所有的bean使用DestructAwareBeanPostProcessor
         registerDisposableBean(beanName,
               new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
      }
      else {
         // 自定义scope...
         Scope scope = this.scopes.get(mbd.getScope());
         if (scope == null) {
            throw new IllegalStateException("No Scope registered for scope name '" + mbd.getScope() + "'");
         }
         scope.registerDestructionCallback(beanName,
               new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
      }
   }
}
```

除了destroy-method方法，还可以注册后置处理器DestructAwareBeanPostProcessor来统一bean的销毁方法

#### 循环依赖

1. 构造器的循环依赖：spring处理不了的，直接抛出BeanCurrentlylnCreationException异常
2. 单例模式下的setter循环依赖：通过“三级缓存”处理循环依赖
3. 非单例循环依赖：无法处理

##### 构造器循环依赖

`singletonsCurrentlylnCreation.add(beanName）`将当前正要创建的bean 记录在缓存中 Spring 容器将每一个正在创建的bean 标识符放在一个“当前创建bean 池”中， bean 标识 柏：在创建过程中将一直保持在这个池中，因此如果在创建bean 过程中发现自己已经在“当前 创建bean 池” 里时，将抛出`BeanCurrentlylnCreationException` 异常表示循环依赖；而对于创建 完毕的bean 将从“ 当前创建bean 池”中清除掉。

##### setter循环依赖

```java
//一级缓存：完成初始化的单例对象的cache
/** Cache of singleton objects: bean name to bean instance. */
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

//二级缓存：完成实例化但是尚未初始化的，提前暴光的单例对象的Cache
/** Cache of early singleton objects: bean name to bean instance. */
private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

//三级缓存：进入实例化阶段的单例对象工厂的cache
/** Cache of singleton factories: bean name to ObjectFactory. */
private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);
```

AB循环依赖问题解决大致逻辑：

1. A 创建过程中需要 B，于是 A 将自己放到三级缓里面 ，去实例化 B
2. B 实例化的时候发现需要 A，于是 B 先查一级缓存，没有，再查二级缓存，还是没有，再查三级缓存，找到了！ 然后把三级缓存里面的这个 A 放到二级缓存里面，并删除三级缓存里面的 A 
3. B 顺利初始化完毕，将自己放到一级缓存里面（此时B里面的A依然是创建中状态） 然后回来接着创建 A，此时 B 已经创建结束，直接从一级缓存里面拿到 B ，然后完成创建，并将自己放到一级缓存里面。

```java
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
  //一级缓存尝试获取 
  Object singletonObject = this.singletonObjects.get(beanName);
   //isSingletonCurrentlyInCreation()判断当前单例bean是否正在创建中
   if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
      synchronized (this.singletonObjects) {
        //二级缓存获取
         singletonObject = this.earlySingletonObjects.get(beanName);
         if (singletonObject == null && allowEarlyReference) {
            ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
            if (singletonFactory != null) {
              //三级缓存获取，并从singletonFactories中移除，并放入earlySingletonObjects中。其实也就是从三级缓存移动到了二级缓存
               singletonObject = singletonFactory.getObject();
               this.earlySingletonObjects.put(beanName, singletonObject);
               this.singletonFactories.remove(beanName);
            }
         }
      }
   }
   return singletonObject;
}
```

```java
protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
   Assert.notNull(singletonFactory, "Singleton factory must not be null");
   synchronized (this.singletonObjects) {
      if (!this.singletonObjects.containsKey(beanName)) {
        //一级缓存存储，移除二级缓存
         this.singletonFactories.put(beanName, singletonFactory);
         this.earlySingletonObjects.remove(beanName);
         this.registeredSingletons.add(beanName);
      }
   }
```

##### 非单例循环依赖

解决不了，Spring 容器不进行缓 存“prototype”作用域的bean ，因此无法提前暴露一个创建中的bean来解决循环依赖问题。



