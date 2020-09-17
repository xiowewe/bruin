#### 创建型

单例模式用来创建全局唯一的对象。工厂模式用来创建不同但是相关类型的对象（继承同一父类或者接口的一组子类），由给定的参数来决定创建哪种类型的对象。建造者模式是用来创建复杂对象，可以通过设置不同的可选参数，“定制化”地创建不同的对象。原型模式针对创建成本比较大的对象，利用对已有对象进行复制的方式进行创建，以达到节省创建时间的目的。

##### 单例模式Singleton

1. 定义

   Singleton 理解起来非常简单。一个类只允许创建一个实例，那这个类就是一个单例类，这种设计模式就叫作单例设计模式，简称单例模式。

2. 用处

   从业务概念上，有些数据在系统中只应该保存一份，就比较适合设计为单例类。比如，系统的配置信息类。除此之外，我们还可以使用单例解决资源访问冲突的问题。

3. 实现

   1. 饿汉式

      ```java
      public class Singleton {
          private static final Singleton INSTANCE = new Singleton();
          private Singleton(){
          }
          public static Singleton getInstance(){
              return INSTANCE;
          }
      }
      ```

   2. 懒汉式

      ```java
      public class Singleton {
          private static Singleton instance = null;
          private Singleton(){
          }
          public static synchronized Singleton getInstance(){
              if(null == instance){
                  instance = new Singleton();
              }
              return instance;
          }
      }
      ```

   3. 双重检测

      ```java
      public class Singleton {
        	//高版本的jdk单例不再需要volatile修饰(这块表示有疑问)
          private static volatile Singleton instance = null;
          private Singleton(){
          }
          public static Singleton getInstance(){
            	//局部变量接受volatile值，避免多次访问主内存 例如：		ReactiveAdapterRegistry、AbstractQueuedSynchronizer
              Singleton temp = instance;
              if(null == temp){
                  synchronized (Singleton.class){
                      if (null == temp){
                          temp = new Singleton();
                          instance = temp;
                      }
                  }
              }
              return temp;
          }
      }
      ```

   4. 静态内部类

      ```java
      public class Singleton {
          private Singleton(){
          }
          public static Singleton getInstance(){
              return SingletonHolder.INSTANCE;
          }
      
          private static class SingletonHolder{
              private static final Singleton INSTANCE = new Singleton();
          }
      }
      ```

   5. 枚举

      ```java
      public class Singleton {
      		INSTANCE;
      }
      ```

      

4. 问题

   1. 对OOP特性支持不太友好
   2. 隐藏类之前的依赖关系
   3. 对代码的拓展性不友好
   4. 代码的可测试性不好
   5. 不支持由参数的构造方案

5. 思考

   1. 如何理解单例模式的唯一性？

      单例是**进程**的单例，单例的作用范围是进程，严格来讲对于Java而言，单例模式的作用范围并非进程而逝类加载器（不同类加载器不能保证单例的唯一性）

   2. 如何实现线程唯一的单例？

      可通过HashMap来实现，ThreadLocal底层实现原理也是通过HashMap

   3. 如何集群环境下的单例？

      把这个单例对象序列化并存储到外部共享存储区

   4. 如何实现一个多例？

6. 应用

   

##### 工厂模式Factory

1. 定义

   简单工厂：也称为静态工厂方法，就是抽离对象和创建，和使用区分开

   工厂方法：定义了一个创建对象的接口，但由子类决定要实例化的类是哪一个。工厂方法让类把实例化推迟到子类

   抽象工厂：提供一个接口，用于创建相关或依赖对象的家族，而不需要明确指定具体类

2. 用处

   1. 封装变化：创建逻辑有可能变化，封装成工厂类之后，创建逻辑的变更对调用者透明。
   2. 代码复用：创建代码抽离到独立的工厂类之后可以复用。
   3. 隔离复杂性：封装复杂的创建逻辑，调用者无需了解如何创建对象。
   4. 控制复杂度：将创建代码抽离出来，让原本的函数或类职责更单一，代码更简洁。

3. 实现

   ```java
   //资源配置解析规则
   public class RuleConfigSource {
     public RuleConfig load(String ruleConfigFilePath) {
       String ruleConfigFileExtension = getFileExtension(ruleConfigFilePath);
       IRuleConfigParser parser = null;
       if ("json".equalsIgnoreCase(ruleConfigFileExtension)) {
         parser = new JsonRuleConfigParser();
       } else if ("xml".equalsIgnoreCase(ruleConfigFileExtension)) {
         parser = new XmlRuleConfigParser();
       } else if ("yaml".equalsIgnoreCase(ruleConfigFileExtension)) {
         parser = new YamlRuleConfigParser();
       } else if ("properties".equalsIgnoreCase(ruleConfigFileExtension)) {
         parser = new PropertiesRuleConfigParser();
       } else {
         throw new InvalidRuleConfigException(
                "Rule config file format is not supported: " + ruleConfigFilePath);
       }
   
       String configText = "";
       //从ruleConfigFilePath文件中读取配置文本到configText中
       RuleConfig ruleConfig = parser.parse(configText);
       return ruleConfig;
     }
   
     private String getFileExtension(String filePath) {
       //...解析文件名获取扩展名，比如rule.json，返回json
       return "json";
     }
   }
   ```

   

   1. 简单工厂

      ```java
      
      
      public class RuleConfigSource {
        public RuleConfig load(String ruleConfigFilePath) {
          String ruleConfigFileExtension = getFileExtension(ruleConfigFilePath);
          IRuleConfigParser parser = RuleConfigParserFactory.createParser(ruleConfigFileExtension);
          if (parser == null) {
            throw new InvalidRuleConfigException(
                    "Rule config file format is not supported: " + ruleConfigFilePath);
          }
      
          String configText = "";
          //从ruleConfigFilePath文件中读取配置文本到configText中
          RuleConfig ruleConfig = parser.parse(configText);
          return ruleConfig;
        }
      
        private String getFileExtension(String filePath) {
          //...解析文件名获取扩展名，比如rule.json，返回json
          return "json";
        }
      }
      
      public class RuleConfigParserFactory {
        public static IRuleConfigParser createParser(String configFormat) {
          IRuleConfigParser parser = null;
          if ("json".equalsIgnoreCase(configFormat)) {
            parser = new JsonRuleConfigParser();
          } else if ("xml".equalsIgnoreCase(configFormat)) {
            parser = new XmlRuleConfigParser();
          } else if ("yaml".equalsIgnoreCase(configFormat)) {
            parser = new YamlRuleConfigParser();
          } else if ("properties".equalsIgnoreCase(configFormat)) {
            parser = new PropertiesRuleConfigParser();
          }
          return parser;
        }
      }
      ```

      单例模式和工厂模式结合

      ```java
      
      public class RuleConfigParserFactory {
        private static final Map<String, RuleConfigParser> cachedParsers = new HashMap<>();
      
        static {
          cachedParsers.put("json", new JsonRuleConfigParser());
          cachedParsers.put("xml", new XmlRuleConfigParser());
          cachedParsers.put("yaml", new YamlRuleConfigParser());
          cachedParsers.put("properties", new PropertiesRuleConfigParser());
        }
      
        public static IRuleConfigParser createParser(String configFormat) {
          if (configFormat == null || configFormat.isEmpty()) {
            return null;//返回null还是IllegalArgumentException全凭你自己说了算
          }
          IRuleConfigParser parser = cachedParsers.get(configFormat.toLowerCase());
          return parser;
        }
      }
      ```

      

   2. 工厂方法

      ```java
      //工厂方法接口
      public interface IRuleConfigParserFactory {
        IRuleConfigParser createParser();
      }
      
      public class JsonRuleConfigParserFactory implements IRuleConfigParserFactory {
        @Override
        public IRuleConfigParser createParser() {
          return new JsonRuleConfigParser();
        }
      }
      
      public class XmlRuleConfigParserFactory implements IRuleConfigParserFactory {
        @Override
        public IRuleConfigParser createParser() {
          return new XmlRuleConfigParser();
        }
      }
      
      public class YamlRuleConfigParserFactory implements IRuleConfigParserFactory {
        @Override
        public IRuleConfigParser createParser() {
          return new YamlRuleConfigParser();
        }
      }
      
      public class PropertiesRuleConfigParserFactory implements IRuleConfigParserFactory {
        @Override
        public IRuleConfigParser createParser() {
          return new PropertiesRuleConfigParser();
        }
      }
      ```

      ```java
      
      public class RuleConfigSource {
        public RuleConfig load(String ruleConfigFilePath) {
          String ruleConfigFileExtension = getFileExtension(ruleConfigFilePath);
      
          IRuleConfigParserFactory parserFactory = RuleConfigParserFactoryMap.getParserFactory(ruleConfigFileExtension);
          if (parserFactory == null) {
            throw new InvalidRuleConfigException("Rule config file format is not supported: " + ruleConfigFilePath);
          }
          IRuleConfigParser parser = parserFactory.createParser();
      
          String configText = "";
          //从ruleConfigFilePath文件中读取配置文本到configText中
          RuleConfig ruleConfig = parser.parse(configText);
          return ruleConfig;
        }
      }
      
      //因为工厂类只包含方法，不包含成员变量，完全可以复用，
      //不需要每次都创建新的工厂类对象，所以，简单工厂模式的第二种实现思路更加合适。
      public class RuleConfigParserFactoryMap { //工厂的工厂
        private static final Map<String, IRuleConfigParserFactory> cachedFactories = new HashMap<>();
      
        static {
          cachedFactories.put("json", new JsonRuleConfigParserFactory());
          cachedFactories.put("xml", new XmlRuleConfigParserFactory());
          cachedFactories.put("yaml", new YamlRuleConfigParserFactory());
          cachedFactories.put("properties", new PropertiesRuleConfigParserFactory());
        }
      
        public static IRuleConfigParserFactory getParserFactory(String type) {
          if (type == null || type.isEmpty()) {
            return null;
          }
          IRuleConfigParserFactory parserFactory = cachedFactories.get(type.toLowerCase());
          return parserFactory;
        }
      }
      ```

   3. 抽象工厂

      解析器类只会根据配置文件格式（Json、Xml、Yaml……）来分类。但是，如果类有两种分类方式，比如，我们既可以按照配置文件格式来分类，也可以按照解析的对象（Rule 规则配置还是 System 系统配置）来分类，那就会对应下面这 8 个 parser 类。

      ```
      针对规则配置的解析器：基于接口IRuleConfigParser
      JsonRuleConfigParser
      XmlRuleConfigParser
      YamlRuleConfigParser
      PropertiesRuleConfigParser
      
      针对系统配置的解析器：基于接口ISystemConfigParser
      JsonSystemConfigParser
      XmlSystemConfigParser
      YamlSystemConfigParser
      PropertiesSystemConfigParser
      ```

      我们可以让一个工厂负责创建多个不同类型的对象，类似生产线上的组装

      ```java
      
      public interface IConfigParserFactory {
        IRuleConfigParser createRuleParser();
        ISystemConfigParser createSystemParser();
        //此处可以扩展新的parser类型，比如IBizConfigParser
      }
      
      public class JsonConfigParserFactory implements IConfigParserFactory {
        @Override
        public IRuleConfigParser createRuleParser() {
          return new JsonRuleConfigParser();
        }
      
        @Override
        public ISystemConfigParser createSystemParser() {
          return new JsonSystemConfigParser();
        }
      }
      
      public class XmlConfigParserFactory implements IConfigParserFactory {
        @Override
        public IRuleConfigParser createRuleParser() {
          return new XmlRuleConfigParser();
        }
      
        @Override
        public ISystemConfigParser createSystemParser() {
          return new XmlSystemConfigParser();
        }
      }
      
      // 省略YamlConfigParserFactory和PropertiesConfigParserFactory代码
      ```

4. 思考

   1. 工厂模式和DI（依赖注入）有什么区别？

      实际上，DI 容器底层最基本的设计思路就是基于工厂模式的。DI 容器相当于一个大的工厂类，负责在程序启动的时候，根据配置（要创建哪些类对象，每个类对象的创建需要依赖哪些其他类对象）事先创建好对象。当应用程序需要使用某个类对象的时候，直接从容器中获取即可。正是因为它持有一堆对象，所以这个框架才被称为“容器”

   2. BeansFactory 是如何设计和实现的，作为一个工厂类能创建所有的对象？

      BeansFactory 创建对象用到的主要技术点就是 Java 中的反射语法：一种动态加载类和创建对象的机制。（具体撸Spring源码）

5. 运用

   

##### 建造者模式Builder

1. 定义

   将一个复杂对象的构建与它的表示分离，使得同样的构建过程可以创建不同的表示。先设置Builder的变量再构建变量，避免对象的短暂无效状态。

2. 实现

   ```java
   
   public class ResourcePoolConfig {
     private String name;
     private int maxTotal;
     private int maxIdle;
     private int minIdle;
   
     private ResourcePoolConfig(Builder builder) {
       this.name = builder.name;
       this.maxTotal = builder.maxTotal;
       this.maxIdle = builder.maxIdle;
       this.minIdle = builder.minIdle;
     }
     //...省略getter方法...
   
     //我们将Builder类设计成了ResourcePoolConfig的内部类。
     //我们也可以将Builder类设计成独立的非内部类ResourcePoolConfigBuilder。
     public static class Builder {
       private static final int DEFAULT_MAX_TOTAL = 8;
       private static final int DEFAULT_MAX_IDLE = 8;
       private static final int DEFAULT_MIN_IDLE = 0;
   
       private String name;
       private int maxTotal = DEFAULT_MAX_TOTAL;
       private int maxIdle = DEFAULT_MAX_IDLE;
       private int minIdle = DEFAULT_MIN_IDLE;
   
       public ResourcePoolConfig build() {
         // 校验逻辑放到这里来做，包括必填项校验、依赖关系校验、约束条件校验等
         if (StringUtils.isBlank(name)) {
           throw new IllegalArgumentException("...");
         }
         if (maxIdle > maxTotal) {
           throw new IllegalArgumentException("...");
         }
         if (minIdle > maxTotal || minIdle > maxIdle) {
           throw new IllegalArgumentException("...");
         }
   
         return new ResourcePoolConfig(this);
       }
   
       public Builder setName(String name) {
         if (StringUtils.isBlank(name)) {
           throw new IllegalArgumentException("...");
         }
         this.name = name;
         return this;
       }
   
       public Builder setMaxTotal(int maxTotal) {
         if (maxTotal <= 0) {
           throw new IllegalArgumentException("...");
         }
         this.maxTotal = maxTotal;
         return this;
       }
   
       public Builder setMaxIdle(int maxIdle) {
         if (maxIdle < 0) {
           throw new IllegalArgumentException("...");
         }
         this.maxIdle = maxIdle;
         return this;
       }
   
       public Builder setMinIdle(int minIdle) {
         if (minIdle < 0) {
           throw new IllegalArgumentException("...");
         }
         this.minIdle = minIdle;
         return this;
       }
     }
   }
   
   // 这段代码会抛出IllegalArgumentException，因为minIdle>maxIdle
   ResourcePoolConfig config = new ResourcePoolConfig.Builder()
           .setName("dbconnectionpool")
           .setMaxTotal(16)
           .setMaxIdle(10)
           .setMinIdle(12)
           .build();
   ```

3. 思考

   1. 建造者模式和工厂模式有什么区别？

      建造者模式是让建造者类来负责对象的创建工作，工厂模式是由工厂类来负责对象创建的工作。实际上，工厂模式是用来创建不同但是相关类型的对象（继承同一父类或者接口的一组子类），由给定的参数来决定创建哪种类型的对象。建造者模式是用来创建一种类型的复杂对象，通过设置不同的可选参数，“定制化”地创建不同的对象。

##### 原型模式Prototype

1. 定义

   如果对象的创建成本比较大，而同一个类的不同对象之间差别不大（大部分字段都相同），在这种情况下，我们可以利用对已有对象（原型）进行复制（或者叫拷贝）的方式，来创建新对象，以达到节省创建时间的目的。这种基于原型来创建对象的方式就叫作原型设计模式，简称原型模式

2. 实现

   两种实现方法，深拷贝和浅拷贝。浅拷贝只会复制对象中基本数据类型数据和引用对象的内存地址，不会递归地复制引用对象，以及引用对象的引用对象……而深拷贝得到的是一份完完全全独立的对象。

3. 思考

   1. BeanUtils是深拷贝？

#### 结构型

##### 代理模式Proxy

1. 定义

   提供对目标对象额外的访问方式，通过代理对象访问目标对象，在不修改原目标的情况下，提供阿魏的功能操作。

2. 实现

   1. 静态代理

      这种代理方式需要代理对象和目标实现同一个接口

      优点：可以在不修改目标对象的前提下扩展目标对象的功能

      缺点：冗余，由于代理对象要实现与目标对象一致的接口，会产生过多的代理类。不易维护。

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

   2. 动态代理

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

   3. cglib代理

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

3. 总结

   1. 静态代理实现较简单，只要代理对象对目标对象进行包装，即可实现增强功能，但静态代理只能为一个目标对象服务，如果目标对象过多，则会产生很多代理类。
   2. JDK动态代理需要目标对象实现业务接口，代理类只需实现InvocationHandler接口。
   3. 动态代理生成的类为 lass com.sun.proxy.\Proxy4，cglib代理生成的类为class com.cglib.UserDao\\EnhancerByCGLIB\\$552188b6。
   4. 静态代理在编译时产生class字节码文件，可以直接使用，效率高。
   5. 动态代理必须实现InvocationHandler接口，通过反射代理方法，比较消耗系统性能，但可以减少代理类的数量，使用更灵活。
   6. cglib代理无需实现接口，通过生成类字节码实现代理，比反射稍快，不存在性能问题，但cglib会继承目标对象，需要重写方法，所以目标对象不能为final类。

4. 思考

   1. 代理模式和装饰模式的却别？

      代理类附加的是跟原始类无关的功能，而在装饰器模式中，装饰器类附加的是跟原始类相关的增强功能，是对功能的增强。

##### 桥接模式Bridge



##### 装饰器模式Decorator

1. 定义

   装饰器模式（Decorator Pattern）允许向一个现有的对象添加新的功能，同时又不改变其结构。主要解决继承关系过于复杂的问题，通过组合来替代继承，装饰器模式还有一个特点，那就是可以对原始类嵌套使用多个装饰器。

   装饰器相对简单的组合关系，有两个比较特殊的地方：

   - 装饰器类和原始类继承同样的父类，这样我们可以对原始类“嵌套”多个装饰器类
   - 装饰器类是对功能的增强

2. 实现

   ```java
   
   // 代理模式的代码结构(下面的接口也可以替换成抽象类)
   public interface IA {
     void f();
   }
   public class A impelements IA {
     public void f() { //... }
   }
   public class AProxy impements IA {
     private IA a;
     public AProxy(IA a) {
       this.a = a;
     }
     
     public void f() {
       // 新添加的代理逻辑
       a.f();
       // 新添加的代理逻辑
     }
   }
   
   // 装饰器模式的代码结构(下面的接口也可以替换成抽象类)
   public interface IA {
     void f();
   }
   public class A impelements IA {
     public void f() { //... }
   }
   public class ADecorator impements IA {
     private IA a;
     public ADecorator(IA a) {
       this.a = a;
     }
     
     public void f() {
       // 功能增强代码
       a.f();
       // 功能增强代码
     }
   }
   ```

3. 应用

   1. Java IO 类的设计思，FileInputStream可以由多个装饰器类进行功能增强，如BufferedInputStream、DataInputStream

   

##### 适配器模式Adapter

1. 定义

   这个模式就是用来做适配的，它将不兼容的接口转换为可兼容的接口，让原本由于接口不兼容而不能一起工作的类可以一起工作

2. 实现

   1. 类适配器：使用继承

      ```java
      
      // 类适配器: 基于继承
      public interface ITarget {
        void f1();
        void f2();
        void fc();
      }
      
      public class Adaptee {
        public void fa() { //... }
        public void fb() { //... }
        public void fc() { //... }
      }
      
      public class Adaptor extends Adaptee implements ITarget {
        public void f1() {
          super.fa();
        }
        
        public void f2() {
          //...重新实现f2()...
        }
        
        // 这里fc()不需要实现，直接继承自Adaptee，这是跟对象适配器最大的不同点
      }
      ```

   2. 对象适配器：使用组合

      ```java
      // 对象适配器：基于组合
      public interface ITarget {
        void f1();
        void f2();
        void fc();
      }
      
      public class Adaptee {
        public void fa() { //... }
        public void fb() { //... }
        public void fc() { //... }
      }
      
      public class Adaptor implements ITarget {
        private Adaptee adaptee;
        
        public Adaptor(Adaptee adaptee) {
          this.adaptee = adaptee;
        }
        
        public void f1() {
          adaptee.fa(); //委托给Adaptee
        }
        
        public void f2() {
          //...重新实现f2()...
        }
        
        public void fc() {
          adaptee.fc();
        }
      }
      ```

      两种实现方式选择：

      - 如果 Adaptee 接口并不多，那两种实现方式都可以。
      - 如果 Adaptee 接口很多，而且 Adaptee 和 ITarget 接口定义大部分都相同，那我们推荐使用类适配器，因为 Adaptor 复用父类 Adaptee 的接口，比起对象适配器的实现方式，Adaptor 的代码量要少一些。
      - 如果 Adaptee 接口很多，而且 Adaptee 和 ITarget 接口定义大部分都不相同，那我们推荐使用对象适配器，因为组合结构相对于继承更加灵活

   3. 应用场景

      1. 封装有缺陷的接口设计
      2. 统一多个类的接口设计
      3. 替换依赖的外部系统
      4. 兼容老版本接口
      5. 适配不同格式的数据

   4. 思考

      1. 代理、桥接、装饰器、适配器 4 种设计模式的区别？

         代理模式：代理模式在不改变原始类接口的条件下，为原始类定义一个代理类，主要目的是控制访问，而非加强功能，这是它跟装饰器模式最大的不同。

         桥接模式：桥接模式的目的是将接口部分和实现部分分离，从而让它们可以较为容易、也相对独立地加以改变。

         装饰器模式：装饰者模式在不改变原始类接口的情况下，对原始类功能进行增强，并且支持多个装饰器的嵌套使用。

         适配器模式：适配器模式是一种事后的补救策略。适配器提供跟原始类不同的接口，而代理模式、装饰器模式提供的都是跟原始类相同的接口。

   5. 应用

      1. slf4J日志框架适配所有其他的日志架构，同时也是门面模式的典型使用
      2. DDD领域驱动中延伸的六边形架构应该就是Adapter模式

##### 门面模式Facade

1. 定义

   门面模式为子系统提供一组统一的接口，定义一组高层接口让子系统更易用。（简单来说就是给细粒度的接口封装成一个统一的接口，提升易用性和性能问题）

2. 应用场景

   1. 解决易用性问题
   2. 解决性能问题
   3. 解决分布式事务问题

3. 思考

   1. 门面模式和适配器模式的区别

      适配器是做接口转换，解决的是原接口和目标接口不匹配的问题。
      门面模式做接口整合，解决的是多接口调用带来的问题

4. 应用

   1. slf4j就是典型的门面模式实现，底层提供了不同的日志实现方式

##### 组合模式Composite

1. 定义

   将一组对象组织成树形结构，以表示一种“部分 - 整体”的层次结构。组合让客户端可以统一单个对象和组合对象的处理逻辑。

   组合模式的设计思路，与其说是一种设计模式，倒不如说是对业务场景的一种数据结构和算法的抽象。其中，数据可以表示成树这种数据结构，业务需求可以通过在树上的递归遍历算法来实现。组合模式，将一组对象组织成树形结构，将单个对象和组合对象都看做树中的节点，以统一处理逻辑，并且它利用树形结构的特点，递归地处理每个子树，依次简化代码实现。使用组合模式的前提在于，你的业务场景必须能够表示成树形结构。所以，组合模式的应用场景也比较局限，它并不是一种很常用的设计模式

2. 应用

   树形结构

   1. 目录-文件
   2. 部门-员工

##### 享元模式Flyweight

1. 定义

   顾名思义就是被共享的单元。享元模式的意图是复用对象，节省内存，前提是享元对象是不可变对象。具体来讲，当一个系统中存在大量重复对象的时候，我们就可以利用享元模式，将对象设计成享元，在内存中只保留一份实例，供多处代码引用，这样可以减少内存中对象的数量，以起到节省内存的目的。

2. 实现

   享元模式的代码实现非常简单，主要是通过工厂模式，在工厂类中，通过一个 Map 或者 List 来缓存已经创建好的享元对象，以达到复用的目的。

   以象棋的棋牌室设计为例，棋子的 id、text、color 都是相同的，唯独 positionX、positionY 不同，可以将棋子的 id、text、color 属性拆分出来，设计成独立的类，并且作为享元供多个棋盘复用。

   ```java
   
   public class ChessPiece {//棋子
     private int id;
     private String text;
     private Color color;
     private int positionX;
     private int positionY;
   
     public ChessPiece(int id, String text, Color color, int positionX, int positionY) {
       this.id = id;
       this.text = text;
       this.color = color;
       this.positionX = positionX;
       this.positionY = positionX;
     }
   
     public static enum Color {
       RED, BLACK
     }
   
     // ...省略其他属性和getter/setter方法...
   }
   
   public class ChessBoard {//棋局
     private Map<Integer, ChessPiece> chessPieces = new HashMap<>();
   
     public ChessBoard() {
       init();
     }
   
     private void init() {
       chessPieces.put(1, new ChessPiece(1, "車", ChessPiece.Color.BLACK, 0, 0));
       chessPieces.put(2, new ChessPiece(2,"馬", ChessPiece.Color.BLACK, 0, 1));
       //...省略摆放其他棋子的代码...
     }
   
     public void move(int chessPieceId, int toPositionX, int toPositionY) {
       //...省略...
     }
   }
   ```

   享元模式设计后的

   ```java
   
   // 享元类
   public class ChessPieceUnit {
     private int id;
     private String text;
     private Color color;
   
     public ChessPieceUnit(int id, String text, Color color) {
       this.id = id;
       this.text = text;
       this.color = color;
     }
   
     public static enum Color {
       RED, BLACK
     }
   
     // ...省略其他属性和getter方法...
   }
   
   public class ChessPieceUnitFactory {
     private static final Map<Integer, ChessPieceUnit> pieces = new HashMap<>();
   
     static {
       pieces.put(1, new ChessPieceUnit(1, "車", ChessPieceUnit.Color.BLACK));
       pieces.put(2, new ChessPieceUnit(2,"馬", ChessPieceUnit.Color.BLACK));
       //...省略摆放其他棋子的代码...
     }
   
     public static ChessPieceUnit getChessPiece(int chessPieceId) {
       return pieces.get(chessPieceId);
     }
   }
   
   public class ChessPiece {
     private ChessPieceUnit chessPieceUnit;
     private int positionX;
     private int positionY;
   
     public ChessPiece(ChessPieceUnit unit, int positionX, int positionY) {
       this.chessPieceUnit = unit;
       this.positionX = positionX;
       this.positionY = positionY;
     }
     // 省略getter、setter方法
   }
   
   public class ChessBoard {
     private Map<Integer, ChessPiece> chessPieces = new HashMap<>();
   
     public ChessBoard() {
       init();
     }
   
     private void init() {
       chessPieces.put(1, new ChessPiece(
               ChessPieceUnitFactory.getChessPiece(1), 0,0));
       chessPieces.put(1, new ChessPiece(
               ChessPieceUnitFactory.getChessPiece(2), 1,0));
       //...省略摆放其他棋子的代码...
     }
   
     public void move(int chessPieceId, int toPositionX, int toPositionY) {
       //...省略...
     }
   }
   ```

3. 思考

   1. 享元模式 vs 单例、缓存、对象池

      应用享元模式是为了对象复用，节省内存，而应用多例模式是为了限制对象的个数。

      缓存，主要是为了提高访问效率，而非复用。

      池化技术中的“复用”可以理解为“重复使用”，主要目的是节省时间（比如从数据库池中取一个连接，不需要重新创建），池化技术是使用者独占，享元模式是共享使用

4. 应用

   1. 实现里面讲的棋牌设计或者文本编辑器设计

   2. jdk中Integer、String的使用

      ```java
      Integer i1 = 56; //自动装箱Integer.valueOf(56), IntegerCache中使用享元模式预先初始化好了-127～128的对象
      Integer i2 = 56;
      Integer i3 = 129;
      Integer i4 = 129;
      System.out.println(i1 == i2); //true
      System.out.println(i3 == i4); //false
      ```



#### 行为型

##### 观察者模式Observer

1. 定义

   在对象之间定义一个一对多的依赖，当一个对象状态改变的时候，所有依赖的对象都会自动收到通知。

2. 实现

   ```java
   
   public interface Subject {
     void registerObserver(Observer observer);
     void removeObserver(Observer observer);
     void notifyObservers(Message message);
   }
   
   public interface Observer {
     void update(Message message);
   }
   
   public class ConcreteSubject implements Subject {
     private List<Observer> observers = new ArrayList<Observer>();
   
     @Override
     public void registerObserver(Observer observer) {
       observers.add(observer);
     }
   
     @Override
     public void removeObserver(Observer observer) {
       observers.remove(observer);
     }
   
     @Override
     public void notifyObservers(Message message) {
       for (Observer observer : observers) {
         observer.update(message);
       }
     }
   
   }
   
   public class ConcreteObserverOne implements Observer {
     @Override
     public void update(Message message) {
       //TODO: 获取消息通知，执行自己的逻辑...
       System.out.println("ConcreteObserverOne is notified.");
     }
   }
   
   public class ConcreteObserverTwo implements Observer {
     @Override
     public void update(Message message) {
       //TODO: 获取消息通知，执行自己的逻辑...
       System.out.println("ConcreteObserverTwo is notified.");
     }
   }
   
   public class Demo {
     public static void main(String[] args) {
       ConcreteSubject subject = new ConcreteSubject();
       subject.registerObserver(new ConcreteObserverOne());
       subject.registerObserver(new ConcreteObserverTwo());
       subject.notifyObservers(new Message());
     }
   }
   ```

3. 使用

   1. Google guava EnventBus
   2. 邮件订阅

##### 模版模式Template

1. 定义

   模板方法模式在一个方法中定义一个算法骨架，并将某些步骤推迟到子类中实现。模板方法模式可以让子类在不改变算法整体结构的情况下，重新定义算法中的某些步骤。

2. 实现

   模板模式经典的实现中，模板方法定义为 final，可以避免被子类重写。需要子类重写的方法定义为 abstract，可以强迫子类去实现。不过，在实际项目开发中，模板模式的实现比较灵活，以上两点都不是必须的

   ```java
   //典型实现，具体实现可以灵活使用
   public abstract class AbstractClass {
     public final void templateMethod() {
       //...
       method1();
       //...
       method2();
       //...
     }
     
     protected abstract void method1();
     protected abstract void method2();
   }
   
   public class ConcreteClass1 extends AbstractClass {
     @Override
     protected void method1() {
       //...
     }
     
     @Override
     protected void method2() {
       //...
     }
   }
   
   public class ConcreteClass2 extends AbstractClass {
     @Override
     protected void method1() {
       //...
     }
     
     @Override
     protected void method2() {
       //...
     }
   }
   
   AbstractClass demo = ConcreteClass1();
   demo.templateMethod();
   ```

3. 使用

   1. Java Servlet、JUnit TestCase、Java InputStream、Java AbstractList

4. 思考

   1. 回调

      实现

      ```java
      
      public interface ICallback {
        void methodToCallback();
      }
      
      public class BClass {
        public void process(ICallback callback) {
          //...
          callback.methodToCallback();
          //...
        }
      }
      
      public class AClass {
        public static void main(String[] args) {
          BClass b = new BClass();
          b.process(new ICallback() { //回调对象
            @Override
            public void methodToCallback() {
              System.out.println("Call back me.");
            }
          });
        }
      }
      ```

      应用

      JdbcTemplate、setClickListener、addShutdownHook

   2. 模板模式 VS 回调

      从应用场景上来看，同步回调跟模板模式几乎一致。它们都是在一个大的算法骨架中，自由替换其中的某个步骤，起到代码复用和扩展的目的。而异步回调跟模板模式有较大差别，更像是观察者模式。

      从代码实现上来看，回调和模板模式完全不同。回调基于组合关系来实现，把一个对象传递给另一个对象，是一种对象之间的关系；模板模式基于继承关系来实现，子类重写父类的抽象方法，是一种类之间的关系。

      组合优于继承。实际上，这里也不例外。在代码实现上，回调相对于模板模式会更加灵活，主要体现在下面几点。

      - 像 Java 这种只支持单继承的语言，基于模板模式编写的子类，已经继承了一个父类，不再具有继承的能力。
      - 回调可以使用匿名类来创建回调对象，可以不用事先定义类；而模板模式针对不同的实现都要定义不同的子类。
      - 如果某个类中定义了多个模板方法，每个方法都有对应的抽象方法，那即便我们只用到其中的一个模板方法，子类也必须实现所有的抽象方法。而回调就更加灵活，我们只需要往用到的模板方法中注入回调对象即可。还记得上一节课的课堂讨论题目吗？看到这里，相信你应该有了答案了吧？

##### 策略模式Strategy

1. 定义

   定义一族算法类，将每个算法分别封装起来，让它们可以互相替换。策略模式可以使算法的变化独立于使用它们的客户端（这里的客户端代指使用算法的代码）

2. 实现

   策略的定义、创建、使用

   1. 定义

      ```java
      
      public interface Strategy {
        void algorithmInterface();
      }
      
      public class ConcreteStrategyA implements Strategy {
        @Override
        public void  algorithmInterface() {
          //具体的算法...
        }
      }
      
      public class ConcreteStrategyB implements Strategy {
        @Override
        public void  algorithmInterface() {
          //具体的算法...
        }
      }
      ```

   2. 创建

      ```java
      
      public class StrategyFactory {
        private static final Map<String, Strategy> strategies = new HashMap<>();
      
        static {
          strategies.put("A", new ConcreteStrategyA());
          strategies.put("B", new ConcreteStrategyB());
        }
      
        public static Strategy getStrategy(String type) {
          if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("type should not be empty.");
          }
          return strategies.get(type);
        }
      }
      ```

   3. 使用

      “运行时动态确定”才是策略模式最典型的应用场景，非运行时确定就变成多态的概念了

      ```java
      public class UserCache {
        private Map<String, User> cacheData = new HashMap<>();
        private EvictionStrategy eviction;
      
        public UserCache(EvictionStrategy eviction) {
          this.eviction = eviction;
        }
      
        //...
      }
      
      // 运行时动态确定，根据配置文件的配置决定使用哪种策略
      public class Application {
        public static void main(String[] args) throws Exception {
          EvictionStrategy evictionStrategy = null;
          Properties props = new Properties();
          props.load(new FileInputStream("./config.properties"));
          String type = props.getProperty("eviction_type");
          evictionStrategy = EvictionStrategyFactory.getEvictionStrategy(type);
          UserCache userCache = new UserCache(evictionStrategy);
          //...
        }
      }
      
      // 非运行时动态确定，在代码中指定使用哪种策略
      public class Application {
        public static void main(String[] args) {
          //...
          EvictionStrategy evictionStrategy = new LruEvictionStrategy();
          UserCache userCache = new UserCache(evictionStrategy);
          //...
        }
      }
      ```

   4. 思考

      1. 当我们添加新的策略的时候，还是需要修改代码，并不完全符合开闭原则

         对于 Java 语言来说，我们可以通过反射来避免对策略工厂类的修改。具体是这么做的：我们通过一个配置文件或者自定义的 annotation 来标注都有哪些策略类；策略工厂类读取配置文件或者搜索被 annotation 标注的策略类，然后通过反射动态地加载这些策略类、创建策略对象；当我们新添加一个策略的时候，只需要将这个新添加的策略类添加到配置文件或者用 annotation 标注即可。

##### 责任链模式

##### 状态模式

##### 迭代器模式

##### 访问者模式

##### 备忘录模式

##### 命宁模式

##### 解释器模式

##### 中介模式