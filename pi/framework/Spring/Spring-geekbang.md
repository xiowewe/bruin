### IOC

#### 重新认识IOC

  	**控制反转**（Inversion of Control），是[面向对象编程](https://zh.wikipedia.org/wiki/面向对象编程)中的一种设计原则，可以用来减低计算机代码之间的[耦合度](https://zh.wikipedia.org/wiki/耦合度_(計算機科學))。其中最常见的方式叫做**依赖注入**（Dependency Injection，简称**DI**），还有一种方式叫“依赖查找”（Dependency Lookup）。通过控制反转，对象在被创建的时候，由一个调控系统内所有对象的外界实体，将其所依赖的对象的引用传递(注入)给它。

#### IOC容器

1. IOC容器实现

   Java SE：Java Beans、Java ServiceLoaded Api、JNDI

   Java EE：EJB、Servlet

   开源项目：Apache Avalon、Google Guice、**Spring Framework**

2. IOC容器特性、职责

   依赖查找、生命周期管理、配置原信息、事件、自定义、资源管理、持久化

3. Spring作为IOC容器的优势

   典型的IOC管理，依赖查找和依赖注入、AOP抽象、事务机制、事件机制、SPI拓展、第三方整合、易测试性、更好的面向对象

### Spring IOC容器

#### 概述

1. 依赖处理

   1. 依赖查找

      1. 实时查找 **BeanFactory#getBean(...)**、延迟查找 **ObjectFactroy#getObject()**
      2. 单个查找 **BeanFactory#getBean(...)**、集合查找 **ListableBeanFactory#getBeansOfType(...)**
      3. 按名称、类型、注解查找 **ListableBeanFactroy#getBeansWithAnnotation(Annotation)**

   2. 依赖注入

      1. byName、byType
      2. setter、constructor、interface、annotation
      3. 实时注入、延迟注入

   3. 依赖来源

      自定义、容器内建Bean对象、容器内建依赖

   4. 依赖查找和依赖注入区别：依赖查找是主动或手动的依赖查找方式，通常需要依赖容器或标准 API 实现。而依赖注入则是手动或自动依赖绑定的方式，无需依赖特定的容器和 API

2. Spring配置原信息

   基于 XML 文件、基于 Properties 文件、基于 Java 注解、基于 Java API(专题讨论)

3. Spring IOC容器

   1. BeanFactory和ApplicationContext区别？

      BeanFactory 是 Spring 底层 IoC 容器，ApplicationContext 是具备应用特性的 BeanFactory 超集。除了IOC容器角色外，还提供面向切面(AOP)、配置元信息(Configuration Metadata)、资源管理(Resources)、事件(Events)、国际化(i18n)、注解(Annotations)
      、Environment 抽象(Environment Abstraction)

   2. BeanFactory、FactoryBean、ObjectFactory区别

      **BeanFactory**：以Factory结尾，表示它是一个工厂类(接口)，用于管理Bean的一个工厂。在Spring中，BeanFactory是IOC容器的核心接口，ApplicationContext也是BeanFactory的衍生容器。

      **FactoryBean**：以Bean结尾，是一个特殊的Bean。实现了FactoryBean<T>接口的Bean，根据该Bean的ID从BeanFactory中获取的实际上是FactoryBean的getObject()返回的对象，而不是FactoryBean本身，如果要获取FactoryBean对象，请在id前面加一个&符号来获取。对于Spring框架来说占有重要地位，第三方框架需要整合Spring时，往往是通过实现FactoryBean来实现的，比如Mybatis的SqlSessionFactoryBean就是通过FactoryBean的特殊性，向Spring容器中注册了一个SqlSessionFactory。

      **ObjectFactory**：普通的工厂对象接口，对于spring在doGetBean时，在于创建对象的过程由框架通过ObjectFactory定义，而创建的时机交给拓展接口Scope，方便拓展自己定义的域。此外在将给依赖注入列表注册一个ObjectFactory类型的对象，在注入过程中会调用objectFactory.getObject()来创建目标对象注入进去。

#### 依赖查找

```java
public interface BeanFactory {
		//名称查找
		Object getBean(String name) throws BeansException;
  	//名称 + 类型查找
		<T> T getBean(String name, Class<T> requiredType) throws BeansException;
		//名称查找，覆盖默认参数
  	Object getBean(String name, Object... args) throws BeansException;
  	//类型查找
		<T> T getBean(Class<T> requiredType) throws BeansException;
  	//类型查找，覆盖默认参数
		<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;
  	//延迟查找
		<T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);
		<T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType);
}
```

##### 名称、类型、延迟查找

1. 名称查找

   源码逻辑`AbstractBeanFactory`#`doGetBean`，Spring 内部缓存了所有的单例 singletonObjects，如果能命中则直接返回，否则需要新创建。

2. 类型查找

   源码逻辑`DefaultListableBeanFactory`#`resolveNamedBean`

   https://www.cnblogs.com/binarylei/p/12302235.html

3. 延迟查找

   BeanObjectProvider 实现了 ObjectProvider 接口，ObjectProvider 接口则是对 ObjectFactory 的扩展。getBeanProvider 内部本质上根据**类型查找**。getBean(Class) 也是通过 resolveBean 方法进行查找。

##### 单个、集合、层次查找

1. 单个查找

   - 单一类型依赖查找接口 - **BeanFactory**
     - 根据 Bean 名称查找
       - getBean(String)
       - Spring 2.5 覆盖默认参数:getBean(String,Object...)
     - 根据 Bean 类型查找
       - Bean 实时查找
         - Spring 3.0 getBean(Class)
         - Spring 4.1 覆盖默认参数:getBean(Class,Object...)
       - Spring 5.1 Bean 延迟查找
         - getBeanProvider(Class)
         - getBeanProvider(ResolvableType)
     - 根据 Bean 名称 + 类型查找:getBean(String,Class)

2. 集合查找

   - 集合类型依赖查找接口 - **ListableBeanFactory** 
     -  根据 Bean 类型查找
       - 获取同类型 Bean 名称列表
         - getBeanNamesForType(Class)
         - Spring 4.2 getBeanNamesForType(ResolvableType)
       - 获取同类型 Bean 实例列表
         - getBeansOfType(Class) 以及重载方法
     - 通过注解类型查找
       - Spring 3.0 获取标注类型 Bean 名称列表
         - getBeanNamesForAnnotation(Class<? extends Annotation>) 
       - Spring 3.0 获取标注类型 Bean 实例列表、
         - getBeansWithAnnotation(Class<? extends Annotation>) 
       -  Spring 3.0 获取指定名称 + 标注类型 Bean 实例
         - findAnnotationOnBean(String,Class<? extends Annotation>)

3. 层次查找

   层次性依赖查找接口 - **HierarchicalBeanFactory**

   - 双亲 BeanFactory:getParentBeanFactory()
   - 层次性查找
     - 根据 Bean 名称查找
     - 基于 containsLocalBean 方法实现
   - 根据 Bean 类型查找实例列表
     - 单一类型:BeanFactoryUtils#beanOfType
     - 集合类型:BeanFactoryUtils#beansOfTypeIncludingAncestors
   - 根据 Java 注解查找名称列表
     - BeanFactoryUtils#beanNamesForTypeIncludingAncestors

##### 内建可查找的依赖

- AbstractApplicationContext 内建可查找的依赖

  | Bean名称                    | Bean实例                        | 使用场景                |
  | --------------------------- | ------------------------------- | ----------------------- |
  | environment                 | Environment对象                 | 外部化配置以及 Profiles |
  | systemProperties            | java.util.Properties 对象       | Java 系统属性           |
  | systemEnvironment           | java.util.Map 对象              | 操作系统环境变量        |
  | messageSource               | MessageSource 对象              | 国际化文案              |
  | lifecycleProcessor          | LifecycleProcessor 对象         | Lifecycle Bean 处理器   |
  | applicationEventMulticaster | ApplicationEventMulticaster对象 | Spring 事件广播器       |

  

- 注解驱动 Spring 应用上下文内建可查找的依赖（部分）

  | Bean名称                                                     | Bean实例                                     | 使用场景                                               |
  | ------------------------------------------------------------ | -------------------------------------------- | ------------------------------------------------------ |
  | org.springframework.context.annotation.internalConfigu rationAnnotationProcessor | ConfigurationClassPostProcessor 对象         | 处理 Spring 配置类                                     |
  | org.springframework.contex t.annotation.internalAutowir edAnnotationProcessor | AutowiredAnnotationBeanPostProcessor 对象    | 处理 @Autowired 以及 @Value 注解                       |
  | org.springframework.contex t.annotation.internalCommo nAnnotationProcessor | CommonAnnotationBeanPostProcessor 对象       | (条件激活)处理 JSR-250 注解， 如 @PostConstruct 等     |
  | org.springframework.contex t.event.internalEventListener Processor | EventListenerMethodProcessor 对象            | 处理标注 @EventListener 的 Spring 事件监听方法         |
  | org.springframework.contex t.event.internalEventListener Factory | DefaultEventListenerFactory 对 象            | @EventListener 事件监听方法适 配为 ApplicationListener |
  | org.springframework.contex t.annotation.internalPersiste nceAnnotationProcessor | PersistenceAnnotationBeanPost Processor 对象 | (条件激活)处理 JPA 注解场景                            |
  | ...                                                          | ...                                          | ...                                                    |

  

#### 依赖注入

##### 依赖注入模式

- 手动模式 - 配置或者编程的方式，提前安排注入规则
  - XML 资源配置元信息
  - Java 注解配置元信息
  - API 配置元信息
- 自动模式 - 实现方提供依赖自动关联的方式，按照內建的注入规则
  - Autowiring(自动绑定)
    - no：默认值，默认为未激活Autowiring
    - byName
    - byType
    - constructor，特殊的byType

##### 依赖注入类型

1. Setter注入

   - 手动模式
     - XML 资源配置元信息
     - Java 注解配置元信息
     - API 配置元信息
   - 自动模式
     - byName
     - byType

2. 构造器注入

   - 手动模式
     - XML 资源配置元信息
     - Java 注解配置元信息
     - API 配置元信息
   - 自动模式
     - constructor

3. 字段注入

   手动模式ava： 注解配置元信息

   - @Autowired（Spring）byType
   - @Resource（JDK）默认byName
   - @Inject（JSR330）byType

4. 方法注入

   手动模式Java： 注解配置元信息

   - @Autowired
   - @Resource
   - @Inject(可选)
   - @Bean

5. 接口注入

   BeanFactoryAware：获取 IoC 容器 - BeanFactory
   ApplicationContextAware：获取 Spring 应用上下文 - ApplicationContext 对象

   EnvironmentAware：获取 Environment 对象
   ResourceLoaderAware：获取资源加载器 对象 - ResourceLoader
   BeanClassLoaderAware：获取加载当前 Bean Class 的 ClassLoader
   BeanNameAware：获取当前 Bean 的名

##### 

#### Spring IOC容器生命周期



### Spring Bean

#### Bean基础

##### BeanDefinition

`BeanDefinition`描述了一个bean实例，它具有属性值，构造函数参数值以及具体实现所提供的更多信息。 这只是一个最顶级基础的接口：主要目的是允许`BeanFactoryPostProcessor`内省和修改属性值和其他Bean元数据。

- BeanDefinition原信息

  | 属性                     | 说明                                          |
  | ------------------------ | --------------------------------------------- |
  | Class                    | Bean 全类名，必须是具体类，不能用抽象类或接口 |
  | Name                     | Bean 的名称或者 ID                            |
  | Scope                    | Bean 的作用域(如:singleton、prototype 等)     |
  | Properties               | Bean 属性设置(用于依赖注入)                   |
  | Constructor arguments    | Bean 构造器参数(用于依赖注入)                 |
  | Autowiring mode          | Bean 自动绑定模式(如:通过名称 byName)         |
  | Lazy initialization mode | Bean 延迟初始化模式(延迟和非延迟)             |
  | Initialization method    | Bean 初始化回调方法名称                       |
  | Destruction method       | Bean 销毁回调方法名称                         |

- BeanDefinition构建

  - 通过BeanDefinitionBuilder
  - 通过AbstractBeanDefinition及其衍生类

##### BeanNameGenerator

​	默认实现`DefaultBeanNameGenerator`，注解Bean命名`AnnotationBeanNameGenerator`

#### Bean作用域

#### Bean生命周期

##### BeanDefinition注册

- Xml配置元信息

- Java注解配置元信息

  @Bean、@Componet、@Import

- Java API配置元信息

  - 命名方式：`BeanDefinitionRegistry`#`registryBeanDefinition(String, BeanDefinition)`
  - 非命名方式：`BeanDefinitionReaderUtils`#`registerWithGeneratedName(AbstractBeanDefinition, BeanDefinitionRegistry)`
  - 配置类方式：`AnnotationBeanDefinitionReader`#`register(Class)`

##### Bean实例化

- 常规方式
  -  通过构造器(配置元信息:XML、Java 注解和 Java API )
  - 通过静态工厂方法(配置元信息:XML 和 Java API )
  - 通过 Bean 工厂方法(配置元信息:XML和 Java API )
  - 通过 FactoryBean(配置元信息:X。ML、Java 注解和 Java API 
- 特殊方式
  - 通过 `ServiceLoaderFactoryBean`(配置元信息:XML、Java 注解和 Java API )
  - 通过 `AutowireCapableBeanFactory`#`createBean(Class, int, boolean)`
  - 通过 `BeanDefinitionRegistry`#`registerBeanDefinition(String,BeanDefinition)`

##### Bean初始化

优先顺序，自上而下

- @PostContract
- `InitializingBean`#`afterPropertiesSet()`
- 自定义初始化
  - Xml配置 init-method
  - Java注解 @Bean(initMethod)
  - Java API `AbstractBeanDefinition`@`setInitMethodName(String)`

##### Bean延迟初始化

- Xml配置 lazy-init
- Java注解@Lazy

##### Bean销毁

- @PreDestroy
- `DisposableBean`#`destroy`
- 自定义销毁
  - Xml配置 destroy
  - Java注解 @Bean(destroy)
  - Java API `AbstractBeanDefinition`@`setDestroyMethod(String)`

### Spring注解

### Spring事务

### Spring配置原信息

### Spring类型转换

### Spring资源管理

### Spring国际化

### Spring事件