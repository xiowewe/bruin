## BeanFactory类结构

### HierarchicalBeanFactory（I）

`HierarchicalBeanFactory`接口定义了`BeanFactory`之间的分层结构，`ConfigurableBeanFactory`中的`setParentBeanFactory`方法能设置父级的`BeanFactory`，下面列出了`HierarchicalBeanFactory`中定义的方法：

```java
// 获取父级的BeanFactory
CopyBeanFactory getParentBeanFactory(); 
// 本地的工厂是否包含指定名字的bean
boolean containsLocalBean(String name);
```

这两个方法都比较直接明了，`getParentBeanFactory`方法用于获取父级`BeanFactory`。`containsLocalBean`用于判断本地的工厂是否包含指定的bean，忽略在祖先工厂中定义的bean。

#### ConfigurableBeanFactory（I）

`ConfigurableBeanFactory`继承自`HierarchicalBeanFactory`和`SingletonBeanRegistry`，提供了bean工厂的配置机制。该BeanFactory接口不适应一般的应用代码中，应该使用`BeanFactory`和`ListableBeanFactory`。该扩展接口仅仅用于内部框架的使用，并且是对bean工厂配置方法的特殊访问。

##### AbstractBeanFactory (A)

`AbstractBeanFactory`继承自`FactoryBeanRegistrySupport`，实现了`ConfigurableBeanFactory`接口。`AbstractBeanFactory`是`BeanFactory`的抽象基础类实现，提供了完整的`ConfigurableBeanFactory`的能力。在这里不讨论该抽象类的实现细节，只要知道这个类是干什么的就行了。

- 单例缓存
- 别名的管理
- FactoryBean的处理
- 用于子bean定义的bean的合并
- bean的摧毁接口
- 自定义的摧毁方法
- BeanFactory的继承管理

子类需要实现的模板方法如下：

```java
Copy// 是否包含给定名字的bean的定义
protected abstract boolean containsBeanDefinition(String beanName);
// 根据bean的名字来获取bean的定义，子类通常要实现缓存
protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;
// 为给定的已经合并了的bean的定义创建bean的实例
protected abstract Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
    throws BeanCreationException;
```



### AutowireCapableBeanFactory（I）

提供了对现有bean进行自动装配的能力，设计目的不是为了用于一般的应用代码中，对于一般的应用代码应该使用`BeanFactory`和`ListableBeanFactory`。其他框架的代码集成可以利用这个接口去装配和填充现有的bean的实例，但是Spring不会控制这些现有bean的生命周期。你也许注意到了`ApplicationContext`中的`getAutowireCapableBeanFactory()`能获取到`AutowireCapableBeanFactory`的实例，也可以实现`BeanFactoryAware`接口来接收`BeanFactory`的实例，然后将其转换成`AutowireCapableBeanFactory`。

#### AbstractAutowireCapatableBeanFactory（A）

`AbstractAutowireCapableBeanFactory`继承自`AbstractBeanFactory`，实现了`AutowireCapableBeanFactory`接口。该抽象了实现了默认的bean的创建。

- 提供了bean的创建、属性填充、装配和初始化
- 处理运行时bean的引用，解析管理的集合、调用初始化方法等
- 支持构造器自动装配，根据类型来对属性进行装配，根据名字来对属性进行装配

### ListableBeanFactory

`ListableBeanFactory`接口有能列出工厂中所有的bean的能力，下面给出该接口中的所有方法：

```java
Copyboolean containsBeanDefinition(String beanName); // 是否包含给定名字的bean的定义
int getBeanDefinitionCount(); // 工厂中bean的定义的数量
String[] getBeanDefinitionNames(); // 工厂中所有定义了的bean的名字
// 获取指定类型的bean的名字
String[] getBeanNamesForType(ResolvableType type);
String[] getBeanNamesForType(Class<?> type);
String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);
// 获取所有使用提供的注解进行标注的bean的名字
String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);
// 查找指定bean中的所有指定的注解（会考虑接口和父类中的注解）
<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
    throws NoSuchBeanDefinitionException;

// 根据指定的类型来获取所有的bean
<T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;
<T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
    throws BeansException;
// 获取所有使用提供的注解进行标注了的bean
Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;
```

上面的这些方法都不考虑祖先工厂中的bean，只会考虑在当前工厂中定义的bean。

- 前八个方法用于获取bean的一些信息
- 最后的三个方法用于获取所有满足条件的bean，返回结果Map中的键为bean的名字，值为bean的实例。这些方法都会考虑通过`FactoryBean`创建的bean，这也意味着`FactoryBean`会被初始化。为什么这里的三个方法不返回List？Map不光包含这些bean的实例，而且还包含bean的名字，而List只包含bean的实例。也就是说Map比List更加的通用。

#### ApplicationContext

#### ConfigurableListableBeanFactory（I）

`ConfigurableListableBeanFactory`接口继承自`ListableBeanFactory`, `AutowireCapableBeanFactory`, `ConfigurableBeanFactory`。大多数具有列出能力的bean工厂都应该实现此接口。此了这些接口的能力之外，该接口还提供了分析、修改bean的定义和单例的预先实例化的机制。这个接口不应该用于一般的客户端代码中，应该仅仅提供给内部框架使用。

##### DefaultListableBeanFactory（C）

`DefaultListableBeanFactory`继承自`AbstractAutowireCapableBeanFactory`，实现了`ConfigurableListableBeanFactory`, `BeanDefinitionRegistry`, `Serializable`接口。这个类是一个非常完全的BeanFactory，基于bean的定义元数据，通过后置处理器来提供可扩展性

XmlBeanFactory

`XmlBeanFactory`继承自`DefaultListableBeanFactory`，用来从XML文档中读取bean的定义的一个非常方便的类。最底层是委派给`XmlBeanDefinitionReader`，实际上等价于带有`XmlBeanDefinitionReader`的`DefaultListableBeanFactory`。



## ApplicationContext类结构

### ConfigurableApplicationContext（I）

`ConfigurableApplicationContext`是比较上层的一个接口，该接口也是比较重要的一个接口，几乎所有的应用上下文都实现了该接口。该接口在`ApplicationContext`的基础上提供了配置应用上下文的能力，此外提供了生命周期的控制能力

#### AbstractApplicationContext（A）

`AbstractApplicationContext`是`ApplicationContext`接口的抽象实现，这个抽象类仅仅是实现了公共的上下文特性。这个抽象类使用了模板方法设计模式，需要具体的实现类去实现这些抽象的方法。

##### GenericApplicationContext（I）

`GenericApplicationContext`继承自`AbstractApplicationContext`，是为通用目的设计的，它能加载各种配置文件，例如xml，properties等等。它的内部持有一个`DefaultListableBeanFactory`的实例，实现了`BeanDefinitionRegistry`接口，以便允许向其应用任何bean的定义的读取器。为了能够注册bean的定义，`refresh()`只允许调用一次。

##### ResourceAdapterApplicationContext（C）

`ResourceAdapterApplicationContext`继承自`GenericApplicationContext`，是为JCA（J2EE Connector Architecture）的ResourceAdapter设计的，主要用于传递`BootstrapContext`的实例给实现了`BootstrapContextAware`接口且由spring管理的bean。覆盖了`postProcessBeanFactory`方法来实现此功能

##### GenericgroovyApplicationContext（C）

`GenericGroovyApplicationContext`继承自`GenericApplicationContext`，实现了`GroovyObject`接口以便能够使用点的语法（.xx）取代`getBean`方法来获取bean。它主要用于Groovy bean的定义，与`GenericXmlApplicationContext`一样，它也能解析XML格式定义的bean。内部使用`GroovyBeanDefinitionReader`来完成groovy脚本和XML的解析。

##### AnnotationConfigApplicationContext（C）

`AnnotationConfigApplicationContext`继承自`GenericApplicationContext`，提供了注解配置（例如：Configuration、Component、inject等）和类路径扫描（scan方法）的支持，可以使用`register(Class<?>... annotatedClasses)`来注册一个一个的进行注册。实现了AnnotationConfigRegistry接口，来完成对注册配置的支持，只有两个方法：register和scan。内部使用`AnnotatedBeanDefinitionReader`来完成注解配置的解析，使用`ClassPathBeanDefinitionScanner`来完成类路径下的bean定义的扫描。

#### AbstractRefreshableApplicationContext（A）

`AbstractRefreshableApplicationContext`继承自`AbstractApplicationContext`，支持多次进行刷新（多次调用`refresh`方法），每次刷新时在内部创建一个新的bean工厂的实例。子类仅仅需要实现`loadBeanDefinitions`方法，该方法在每次刷新时都会调用。

##### AbstractRefreshableConfigApplicationContext

`AbstractRefreshableConfigApplicationContext`继承自`AbstractRefreshableApplicationContext`，添加了对指定的配置文件路径的公共的处理，可以把他看作基于XML的应用上下文的基类。实现了如下的两个接口：

- `BeanNameAware`用于设置上下文的bean的名称，只有一个方法：`void setBeanName(String name)`
- `InitializingBean`用于上下文一切就绪后，如果还未刷新，那么就执行刷新操作，只有一个方法：`void afterPropertiesSet()`

##### ClassPathXmlApplicationContext

`ClassPathXmlApplicationContext`继承自`AbstractXmlApplicationContext`，和`FileSystemXmlApplicationContext`类似，只不过`ClassPathXmlApplicationContext`是用于处理类路径下的XML配置文件。文件的路径可以是具体的文件路径，例如：xxx/application.xml，也可以是ant风格的配置，例如：xxx/*-context.xml。

### WebApplicationContext（I）

#### ConfigurableWebApplicationContext

#### GenericWebApplicationContext

#### AbstractRefreshableWebApplicationContext

#### GroovyWebApplicationContext

#### AnnotationConfigWebApplicationContext









### WebServerApplicationContext（I）

### ReactiveWebApplicationContext（I）

### ApplicationContextAssertProvider（I）



IOC容器解析：https://www.cnblogs.com/zhangfengxian/p/11086695.html

BeanFactory类结构：https://www.cnblogs.com/zhangfengxian/p/11296591.html

ApplicationContext类结构：https://www.cnblogs.com/zhangfengxian/archive/2004/01/13/11192054.html