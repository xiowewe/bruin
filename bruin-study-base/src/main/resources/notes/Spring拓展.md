#### NamespaceHandler

通过自定义NamespaceHanlder，再配合自定义的BeanDefinitionParser，可以对自定义Bean的组装操作，对于BeanDefinitiion的数据结构，进行个性化创建。Spring中对NamespaceHandler有很多拓展实现，例如：

1. TxNamespaceHandler实现对“annotation-driven”等属性自定义解析，
2. NamespaceHandler实现对“scan”属性结合MapperScannerBeanDefinitionParser进行自定义解析实现mapper文件的扫描
3. DubboNamespaceHandler实现对“consumer”，“service”,"reference"等属性结合DubboBeanDefinitionParser实现自定义解析，dubbo项目下配置META-INF\dubbo.xsd

##### 自定义NamespaceHandler实现

- UserNamespaceHandler

  实现**NamespaceHandlerSupport**或者直接实现NamespaceHandler接口，为element注册自定义的BeanDefinitionParser

- UserBeanDefinitionParser

  自定义BeanDefnition解析器，实现自定义的解析规则

- 设置UserNamespaceHandler路径和xsd解析规则

  META-INF文件下增加spring.handlers、spring.schemas文件

##### 实现原理

自定义Bean的加载路径分为两个入口：xml 类型BeanDefinition、Annotation类型BeanDefintion注册过程，前者入口是 **obtainFreshBeanFactory()**方法，后者**invokeBeanFactoryPostProcessors()**。Springboot通过后者实现configurationClass BeanDefinition的注册，关于自定义解析两者的统一入口方法：AbstractBeanDefinition#loadBeanDefinition()

```java
//调用链路
AbstractBeanDefinition#loadBeanDefinition();
XmlBeanDefinitionReader#registerBeanDefintion();
DefaultBeanDefinitionDocumentReader#doRegisterBeanDefinitions();
DefaultBeanDefinitionDocumentReader#parseBeanDefinitions();
BeanDefinitionParserDelegate#parseCustomElement();

```

```java
public BeanDefinition parseCustomElement(Element ele, BeanDefinition containingBd) {
		String namespaceUri = getNamespaceURI(ele);
		if (namespaceUri == null) {
			return null;
		}
    	//或者spring.handlers配置的自定义NamespaceHandler
		NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
		if (handler == null) {
			error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
			return null;
		}
    	// 通过它parse 对象. 得到BeanDefinition
		return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
	}
```



#### BeanFactoryPostProcessor

​	详见 **BeanFactoryPostProcessor.md**

#### BeanPostProcessor

​	详见 **BeanPostProcessor.md**

#### InitializingBean、DisposableBean

```java
//InitializingBean 接口,bean属性设置完毕之后调用该方法做一些初始化的工作
void afterPropertiesSet() throws Exception;

//DisposableBean 接口，bean生命周期结束之前执行该方法做一些收尾工作
void destroy() throws Exception;
```

激活Bean的初始化方法有三种实现，分别是@PostConstrct、实现InitializingBean#afterPropertiesSet方法、@InitMethod注解的方法，三者初始化执行顺序也是按照前面描述的先后顺序执行。@PostConstrct 注解的方法是通过CommonAnnotationBeanPostProcessor 后处理器在初始化之前后处理器执行的时候触发，后两者是在初始化属性填充之后invokeInitMethods()方法中出发，源码如下（Bean的销毁同样也有三种实现，顺序如下@PreDestroy--》 Destroyable#destroy --》 destroy—method，实现原理同初始化）

```java
protected void invokeInitMethods(String beanName, final Object bean, RootBeanDefinition mbd) throws Throwable {
    boolean isInitializingBean = bean instanceof InitializingBean;
    if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
        }

        if (System.getSecurityManager() != null) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    public Object run() throws Exception {
                        ((InitializingBean)bean).afterPropertiesSet();
                        return null;
                    }
                }, this.getAccessControlContext());
            } catch (PrivilegedActionException var6) {
                throw var6.getException();
            }
        } else {
          	//执行InitializingBean 的afterPropertiesSet接口
            ((InitializingBean)bean).afterPropertiesSet();
        }
    }

    if (mbd != null) {
        String initMethodName = mbd.getInitMethodName();
        if (initMethodName != null && (!isInitializingBean || !"afterPropertiesSet".equals(initMethodName)) && !mbd.isExternallyManagedInitMethod(initMethodName)) {
            //执行自定义的@InitMethod注解的方法（init-method）
            this.invokeCustomInitMethod(beanName, bean, mbd);
        }
    }

}
```

InitializingBean、DisposableBean应用比较广泛，Dubbo 的serviceBean暴露（export）和销毁（unExport）过程就应用到。



#### *Aware接口

激活Aware方法，实现这些接口的Bean在初始化之后可以获得一些资源，比如BeanNameAware 可以获取BeanName，BeanFactoryAware注入BeanFactory实例，以及ApplicationContextAware 会注入ApplicationContext实例等

接口实现的依次顺序:（该方法实现前三者，后者的实现在Bean初始化前置处理其中实现）
BeanNameAware、BeanClassLoaderAware、BeanFactoryAware、EnviromentAware、EmbeddedValueResolverAware、ResourceLoarderAware、ApplicationEnventPublisherAware、 MessageSourceAware、ApplicationContextAware

1. initializeBean方法中invokeAwareMethods

   ```java
   private void invokeAwareMethods(String beanName, Object bean) {
       if (bean instanceof Aware) {
           if (bean instanceof BeanNameAware) {
               ((BeanNameAware)bean).setBeanName(beanName);
           }
   
           if (bean instanceof BeanClassLoaderAware) {
               ((BeanClassLoaderAware)bean).setBeanClassLoader(this.getBeanClassLoader());
           }
   
           if (bean instanceof BeanFactoryAware) {
               ((BeanFactoryAware)bean).setBeanFactory(this);
           }
       }
   
   }
   ```

2. ApplicationContextAwareProcessor后处理器执行

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
         invokeAwareInterfaces(bean);
      }
   
      return bean;
   }
   ```



#### FactoryBean

FactoryBean和BeanFactory虽然名字很像，但是这两者是完全不同的两个概念，用途上也是天差地别。BeanFactory是一个Bean工厂，在一定程度上我们可以简单理解为它就是我们平常所说的Spring容器(注意这里说的是简单理解为容器)，它完成了Bean的创建、自动装配等过程，存储了创建完成的单例Bean。而FactoryBean通过名字看，是一个工厂Bean，开发者可以定制化自己想要的实例Bean

FactoryBean的特殊之处在于它可以向容器中注册两个Bean，一个是它本身，一个是FactoryBean.getObject()方法返回值所代表的Bean。

##### 自定义FactoryBean

```java
package com.bruin.expansion.spring.factorybean.componet;

public class TestFactoryBean {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(TestFactoryBean.class);

        System.out.println(applicationContext.getBean(TestService.class));

      	//注意，这里返回的并不是DemoFactoryBean示例，而是它getObject()返回的实例
        System.out.println(applicationContext.getBean("demoFactoryBean"));
				//需要前面增加 &
        System.out.println(applicationContext.getBean("&demoFactoryBean"));

    }
}

//testService construct
//com.bruin.expansion.spring.factorybean.service.TestService@7ee955a8
//com.bruin.expansion.spring.factorybean.service.TestService@7ee955a8
//com.bruin.expansion.spring.factorybean.componet.DemoFactoryBean@1677d1
```

```java
package com.bruin.expansion.spring.factorybean.componet;

@Component
public class DemoFactoryBean implements FactoryBean {

    @Override
    public Object getObject() throws Exception {
        return new TestService();
    }

    @Override
    public Class<?> getObjectType() {
        return TestService.class;
    }
}

//TestService 代码在service 包下，省略。。。
```

##### FactoryBean源码

- 在容器启动阶段，会先通过getBean()方法来创建DemoFactoryBean的实例对象。如果实现了SmartFactoryBean接口，且isEagerInit()方法返回的是true，那么在容器启动阶段，就会调用getObject()方法，向容器中注册getObject()方法返回值的对象。否则，只有当第一次获取getObject()返回值的对象时，才会去回调getObject()方法

  ```java
  //finishBeanFactoryInitialization 初始化所有非non-lazy bean 调用此方法
  public void preInstantiateSingletons() throws BeansException {
     if (logger.isTraceEnabled()) {
        logger.trace("Pre-instantiating singletons in " + this);
     }
  
     //获取容器所有的beanName
     List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);
     for (String beanName : beanNames) {
        RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
        if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
           //根据beanName判断bean是不是一个FactoryBean
           if (isFactoryBean(beanName)) {
              Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
              if (bean instanceof FactoryBean) {
                //获取或者创建加 & 的单例对象
                 final FactoryBean<?> factory = (FactoryBean<?>) bean;
                 boolean isEagerInit;
                 if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
                    isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
                                ((SmartFactoryBean<?>) factory)::isEagerInit,
                          getAccessControlContext());
                 }
                 else {
                    //如果实现SmartFactoryBean接口，则在容器初始化时就向容器中注册getObject()方法返回值的对象
                    isEagerInit = (factory instanceof SmartFactoryBean &&
                          ((SmartFactoryBean<?>) factory).isEagerInit());
                 }
                 if (isEagerInit) {
                    getBean(beanName);
                 }
              }
           }
           else {
              getBean(beanName);
           }
        }
     }
  
     // 所有beanName 触发SmartInitializingSingleton的回调方法
     for (String beanName : beanNames) {
        Object singletonInstance = getSingleton(beanName);
        if (singletonInstance instanceof SmartInitializingSingleton) {
           final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
           if (System.getSecurityManager() != null) {
              AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                 smartSingleton.afterSingletonsInstantiated();
                 return null;
              }, getAccessControlContext());
           }
           else {
              smartSingleton.afterSingletonsInstantiated();
           }
        }
     }
  }
  ```

- 在getObjectForBeanInstance()方法中会先判断bean是不是FactoryBean，如果不是，就直接返回Bean。如果是FactoryBean，且name是以&符号开头，那么表示的是获取FactoryBean的原生对象，也会直接返回。如果name不是以&符号开头，那么表示要获取FactoryBean中getObject()方法返回的对象。会先尝试从FactoryBeanRegistrySupport类的factoryBeanObjectCache这个缓存map中获取，如果缓存中存在，则返回，如果不存在，则去调用getObjectFromFactoryBean()方法。

  

  源码不贴了，参考：https://juejin.im/post/5d8e06b06fb9a04e1c07d87b

##### Factory Bean应用

- MapperFactoryBean

  获取mapper接口实例对象

- SqlSessionFactoryBean

  通过buildSqlSessionFactoryBean	获取SqlSessionFactoryBean

- dubbo  ReferenceBean

  获取reference接口实例对象