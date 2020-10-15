#### 代理 Proxy

##### Static Proxy

这种代理方式需要代理对象和目标实现同一个接口。

- 优点：可以在不修改目标对象的前提下扩展目标对象的功能
- 缺点：冗余，由于代理对象要实现与目标对象一致的接口，会产生过多的代理类。不易维护。

```java
//接口
public interface IBuyHouse {
    void buyHouse();
}

//目标类
public class Consumer implements IBuyHouse {

    @Override
    public void buyHouse() {
        System.out.println("consumer buy house");
    }
}

//静态代理类
public class StaticProxy implements IBuyHouse {

    private Consumer consumer;

    public StaticProxy(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void buyHouse() {
        System.out.println("staticProxy advice house");
        consumer.buyHouse();
        System.out.println("staticProxy charge fee");
    }
}
```

##### Dynamic Proxy

动态代理利用了[JDK API](http://tool.oschina.net/uploads/apidocs/jdk-zh/)，动态地在内存中构建代理对象，从而实现对目标对象的代理功能。静态代理与动态代理的区别主要在：

- 静态代理在编译时就已经实现，编译完成后代理类是一个实际的class文件
- 动态代理是在运行时动态生成的，即编译完成后没有实际的class文件，而是在运行时动态生成类字节码，并加载到JVM中

**特点：**
动态代理对象不需要实现接口，但是要求目标对象必须实现接口，否则不能使用动态代理

```java
//目标类没有实现IBuyHouse接口的情况下
public class DynamicProxyFactory {
    private Object target;

    public DynamicProxyFactory(Object consumer) {
        this.target = consumer;
    }

    public Object getProxyInstance(){
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                //InvocationHandler lambda表达式
                (proxy, method, args) -> {
                    System.out.println("dynamicProxy advice house");
                    Object object = method.invoke(target, args);
                    System.out.println("dynamicProxy charge fee");

                    return object;
                });
    }
}
```

##### CGLIB Proxy

[cglib](https://github.com/cglib/cglib) (Code Generation Library )是一个第三方代码生成类库，运行时在内存中动态生成一个子类对象从而实现对目标对象功能的扩展。

**cglib特点**

- JDK的动态代理有一个限制，就是使用动态代理的对象必须实现一个或多个接口。
  如果想代理没有实现接口的类，就可以使用CGLIB实现。
- CGLIB是一个强大的高性能的代码生成包，它可以在运行期扩展Java类与实现Java接口。
  它广泛的被许多AOP的框架使用，例如Spring AOP和dynaop，为他们提供方法的interception（拦截）。
- CGLIB包的底层是通过使用一个小而快的字节码处理框架ASM，来转换字节码并生成新的类。
  不鼓励直接使用ASM，因为它需要你对JVM内部结构包括class文件的格式和指令集都很熟悉。

cglib与动态代理最大的**区别**就是

- 使用动态代理的对象必须实现一个或多个接口
- 使用cglib代理的对象则无需实现接口，达到代理类无侵入。

```java
public class CglibProxyFactory implements MethodInterceptor {

    private Object target;

    public CglibProxyFactory(Object target) {
        this.target = target;
    }

    public Object getProxyInstance(){
        Enhancer enhancer = new Enhancer();
        //设置父类
        enhancer.setSuperclass(target.getClass());
        //设置回调
        enhancer.setCallback(this);

        //创建子类对象代理
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("cglibProxy advice house");
        Object object = method.invoke(target, objects);
        System.out.println("cglibProxy charge fee");

        return object;
    }
}
```

测试类

```java
public class Test {

    public static void main(String[] args) {
        Consumer target = new Consumer();
        //静态代理
        IBuyHouse buyHouse = new StaticProxy(target);
        buyHouse.buyHouse();


        //动态代理
        IBuyHouse dynamicProxy = (IBuyHouse) new DynamicProxyFactory(target).getProxyInstance();
        dynamicProxy.buyHouse();

        //cglib代理
        Consumer cglibProxy = (Consumer) new CglibProxyFactory(target).getProxyInstance();
        cglibProxy.buyHouse();
    }
}
```

1. 



#### Spring AOP

AOP的实现原理：动态（CGLIB）代理 + BeanPostProcessor

##### AOP代理创建

`AbstractAutowireCapableBeanFactory`#`initializeBean`，在bean初始化过程中，初始化后bean后置处理方法即为AOP代理创建的入口。

```java
protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
  	
  	//对特殊的bean处理，Aware、BeanNameAware、BeanClassLoaderAware、BeanFactoryAware
    invokeAwareMethods(beanName, bean);
   

   Object wrappedBean = bean;
   if (mbd == null || !mbd.isSynthetic()) {
     //初始化前应用后置处理器
      wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
   }

   //激活InitializingBean、init-method用户自定义初始化方法
   invokeInitMethods(beanName, wrappedBean, mbd);
   
  //初始化后应用后置处理器
   if (mbd == null || !mbd.isSynthetic()) {
      wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
   }

   return wrappedBean;
}
```

`AbstractAutoProxyCreator`  这个抽象类实现了`BeanPostProcessor`,同样也会执行`postProcessAfterInitialization`方法

```java
public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
   if (bean != null) {
     //根据给定bean的class和name构建一个key，格式className_beanName
      Object cacheKey = getCacheKey(bean.getClass(), beanName);
      if (this.earlyProxyReferences.remove(cacheKey) != bean) {
        //如果必要则封装制定的bean
         return wrapIfNecessary(bean, beanName, cacheKey);
      }
   }
   return bean;
}
```

```java
protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
  //已经处理过 
  if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
      return bean;
   }
  //无需增强
   if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
      return bean;
   }
  //给定类是否为一个基础类，基础类不应代理
   if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
      this.advisedBeans.put(cacheKey, Boolean.FALSE);
      return bean;
   }

   // 获取增强方法
   Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
  //如果获取到了增强则需要针对增强创建代理
   if (specificInterceptors != DO_NOT_PROXY) {
      this.advisedBeans.put(cacheKey, Boolean.TRUE);
     //创建代理
      Object proxy = createProxy(
            bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
      this.proxyTypes.put(cacheKey, proxy.getClass());
      return proxy;
   }

   this.advisedBeans.put(cacheKey, Boolean.FALSE);
   return bean;
}
```

1. 获取增强器、寻找匹配增强器

   ```java
   protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
     //1、获取增强器
     // BeanFactoryAspectJAdvisorsBuilder#buildAspectJAdvisors 遍历所有beanName获取 isAspect 的beanName通过getAdvisors 获取他的增强器，包括@Before、@After、@Around
      List<Advisor> candidateAdvisors = findCandidateAdvisors();
     //2、寻找所有增强器中适合桑钱class的增强器
      List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
      extendAdvisors(eligibleAdvisors);
      if (!eligibleAdvisors.isEmpty()) {
         eligibleAdvisors = sortAdvisors(eligibleAdvisors);
      }
      return eligibleAdvisors;
   }
   ```

2. 创建代理

```java
protected Object createProxy(Class<?> beanClass, @Nullable String beanName,
      @Nullable Object[] specificInterceptors, TargetSource targetSource) {

   if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
      AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
   }

   ProxyFactory proxyFactory = new ProxyFactory();
  //获取当前类的属性
   proxyFactory.copyFrom(this);
//检查proxyTargetClass设置
   if (!proxyFactory.isProxyTargetClass()) {
      if (shouldProxyTargetClass(beanClass, beanName)) {
         proxyFactory.setProxyTargetClass(true);
      }
      else {
         evaluateProxyInterfaces(beanClass, proxyFactory);
      }
   }

  //获取加入增强器
   Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
   proxyFactory.addAdvisors(advisors);
  //设置要代理的类
   proxyFactory.setTargetSource(targetSource);
  //定制代理
   customizeProxyFactory(proxyFactory);

   proxyFactory.setFrozen(this.freezeProxy);
   if (advisorsPreFiltered()) {
      proxyFactory.setPreFiltered(true);
   }

  //proxy工厂类创建代理
   return proxyFactory.getProxy(getProxyClassLoader());
}
```

- 代理类型判断

  - optimize 控制通过CGLIB创建的代理是否使用激进的优化策略
  - proxyTargetClass   目标类本身被代理而不是目标类的接口，true时创建CGLIB代理
  - hasNoUserSuppliedProxyInterface 是否存在代理接口

- JdkDynamicAopProxy

  ```java
  public class DynamicProxyFactory {
      private Object target;
    public DynamicProxyFactory(Object consumer) {
        this.target = consumer;
    }
  
    public Object getProxyInstance(){
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                //InvocationHandler lambda表达式, invoke方法
                (proxy, method, args) -> {
                    System.out.println("dynamicProxy advice house");
                    Object object = method.invoke(target, args);
                    System.out.println("dynamicProxy charge fee");
  
                  return object;
              	});
  	}
  }
  ```

  - 动态代理实现demo如上，主要包括三个函数：

    - 构造函数，将代理的对象注入
    - invoke方法，实现AOP增强
    - getProxy方法，千遍一律

  - JdkDynamicAopProxy实现

    - getProxy方法

      ```java
      public Object getProxy(@Nullable ClassLoader classLoader) {
         if (logger.isTraceEnabled()) {
            logger.trace("Creating JDK dynamic proxy: " + this.advised.getTargetSource());
         }
         Class<?>[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised, true);
         findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
         return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
      }
      ```

    - invoke方法

      先构建链chain，然后封装次链进行串联调用（**ReflectiveMethodInvocation**）

      ```java
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         Object oldProxy = null;
         boolean setProxyContext = false;
      
         TargetSource targetSource = this.advised.targetSource;
         Object target = null;
      
         try {
            if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
               // The target does not implement the equals(Object) method itself.
               return equals(args[0]);
            }
            else if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
               // The target does not implement the hashCode() method itself.
               return hashCode();
            }
            else if (method.getDeclaringClass() == DecoratingProxy.class) {
               // There is only getDecoratedClass() declared -> dispatch to proxy config.
               return AopProxyUtils.ultimateTargetClass(this.advised);
            }
            else if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
                  method.getDeclaringClass().isAssignableFrom(Advised.class)) {
               // Service invocations on ProxyConfig with the proxy config...
               return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
            }
      
            Object retVal;
      
            if (this.advised.exposeProxy) {
               // Make invocation available if necessary.
               oldProxy = AopContext.setCurrentProxy(proxy);
               setProxyContext = true;
            }
      
            // Get as late as possible to minimize the time we "own" the target,
            // in case it comes from a pool.
            target = targetSource.getTarget();
            Class<?> targetClass = (target != null ? target.getClass() : null);
      
            // Get the interception chain for this method.
            List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
      
            // Check whether we have any advice. If we don't, we can fallback on direct
            // reflective invocation of the target, and avoid creating a MethodInvocation.
            if (chain.isEmpty()) {
               // We can skip creating a MethodInvocation: just invoke the target directly
               // Note that the final invoker must be an InvokerInterceptor so we know it does
               // nothing but a reflective operation on the target, and no hot swapping or fancy proxying.
               Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
               retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
            }
            else {
               // We need to create a method invocation...
               MethodInvocation invocation =
                     new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
               // Proceed to the joinpoint through the interceptor chain.
               retVal = invocation.proceed();
            }
      
            // Massage return value if necessary.
            Class<?> returnType = method.getReturnType();
            if (retVal != null && retVal == target &&
                  returnType != Object.class && returnType.isInstance(proxy) &&
                  !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
               // Special case: it returned "this" and the return type of the method
               // is type-compatible. Note that we can't help if the target sets
               // a reference to itself in another returned object.
               retVal = proxy;
            }
            else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
               throw new AopInvocationException(
                     "Null return value from advice does not match primitive return type for: " + method);
            }
            return retVal;
         }
         finally {
            if (target != null && !targetSource.isStatic()) {
               // Must have come from TargetSource.
               targetSource.releaseTarget(target);
            }
            if (setProxyContext) {
               // Restore old proxy.
               AopContext.setCurrentProxy(oldProxy);
            }
         }
      }
      ```

    - CglibAopProxy 

      与JDK方法实现代理中额invoke方法大同小异，先构建链chain，然后封装次链进行串联调用，只不过JDK中直接构造ReflectiveMethodInvocation，而CGLIB使用CglibMethodInvocation（继承自ReflectiveMethodInvocation，但是processed方法并没有重写）

      

##### AOP家庭成员

##### PointCut

PointCut 依赖了ClassFilter和MethodMatcher,ClassFilter用来指定特定的类，MethodMatcher 指定特定的函数，正是由于PointCut仅有的两个依赖，它只能实现函数级别的AOP。对于属性、for语句等是无法实现该切点的。
MethodMatcher 有两个实现类StaticMethodMatcher和DynamicMethodMatcher，它们两个实现的唯一区别是isRuntime(参考源码)。StaticMethodMatcher不在运行时检测，DynamicMethodMatcher要在运行时实时检测参数，这也会导致DynamicMethodMatcher的性能相对较差。

##### Advise

- AfterAdvice是指函数调用结束之后增强，它又包括两种情况：异常退出和正常退出；
- BeforeAdvice指函数调用之前增强；
- Inteceptor有点特殊，它是由AOP联盟定义的标准，也是为了方便Spring AOP 扩展，以便对其它AOL支持。Interceptor有很多扩展，比如Around Advice的功能实现

##### Advisor

同样Advisor按照Advice去分也可以分成两条线路，一个是来源于Spring AOP 的类型，一种是来自AOP联盟的Interceptoor, IntroductionAdvisor就是对MethodInterceptor的继承和实现



#### 实践

Authentication 权限
Caching 缓存
Context passing 内容传递
Error handling 错误处理
Lazy loading　懒加载
Debugging　　调试
logging, tracing, profiling and monitoring　记录跟踪　优化　校准
Performance optimization　性能优化
Persistence　　持久化
Resource pooling　资源池
Synchronization　同步
Transactions 事务

