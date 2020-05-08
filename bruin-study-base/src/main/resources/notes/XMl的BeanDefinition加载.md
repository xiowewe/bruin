#### **AbstractXmlApplicationContext**容器 

##### BeanDefinition解析、注册

1. **调用链路**

   ```
   AbstractApplicationContext#refre -->
   	AbstractApplicationContext#obtainFreshBeanFactory   -->
   		AbstrcatRefreshableApplicationContext#refreshBeanFactory
   ```

   refreshBeanFactory()源码逻辑

   ```java
   protected final void refreshBeanFactory() throws BeansException {
      if (hasBeanFactory()) {
         destroyBeans();
         closeBeanFactory();
      }
      try {
         //创建BeanFactory
         DefaultListableBeanFactory beanFactory = createBeanFactory();
         beanFactory.setSerializationId(getId());
         //自定义此上下文使用的内部bean工厂
         customizeBeanFactory(beanFactory);
         //BeanDefinition加载
         //抽象方法有多种实现：
         //1、AbstractXmlApplicationContext，基于XML形式的BeanDefinition解析、注册
         //2、AnnotationConfigWebApplicationContext，基于注解形式的BeanDefinition解析、注册
         //3、GroovyWebApplicationContext
         //4、XmlWebApplicationContext
         loadBeanDefinitions(beanFactory);
         synchronized (this.beanFactoryMonitor) {
            this.beanFactory = beanFactory;
         }
      }
      catch (IOException ex) {
         throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
      }
   }
   ```

2. **AbstractXmlApplicationContext**#loadBeanDefinitions

   1. 创建和初始化**XMLBeanDefinitionReader**

   2. 调用重载方法loadBeanDefinitions(XmlBeanDefinitionReader reader)

      configResources、configlocations判空

   ```java
   protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
   		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
       	//创建和初始化**XMLBeanDefinitionReader**
   		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
   
   		// Configure the bean definition reader with this context's
   		// resource loading environment.
   		beanDefinitionReader.setEnvironment(this.getEnvironment());
   		beanDefinitionReader.setResourceLoader(this);
   		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
   
   		// Allow a subclass to provide custom initialization of the reader,
   		// then proceed with actually loading the bean definitions.
   		initBeanDefinitionReader(beanDefinitionReader);
           //configResources、configlocations判空
   		loadBeanDefinitions(beanDefinitionReader);
   	}
   ```

   

3. **AbstactBeanDefinitionReader**#loadBeanDefinitions(String ...locations) 实现BeanDefinitionReader

   加载beanDefinition 累计计数

   ```java
   public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
   		Assert.notNull(resources, "Resource array must not be null");
   		int count = 0;
   		for (Resource resource : resources) {
               //累计计数
   			count += loadBeanDefinitions(resource);
   		}
   		return count;
   	}
   ```

   

4. **AbstactBeanDefinitionReader**#loadBeanDefinitions(String location)

   1. 获取资源resource
   2. 调用**AbstactBeanDefinitionReader**#loadBeanDefinitions(Resource ... resource) 

5. **XmlBeanDefinitionReader**#loadBeanDefinitions(Resource resource) 实现**BeanDefinitionReader**接口

   1. 封装资源文件EncodedResource
   2. 获取InputStream输出流
   3. 获取InputSource

6. **XmlBeanDefinitionReader**#doLoadBeanDefinitions

   1. 获取对XML文件验证模式（DTD 和 XSD的区别）
   2. doLoadDocument加载XML文件，得到对应的Document
   3. registerBeanDefinitions 注册BeanDefinition

7. **XmlBeanDefinitionReader**#registerBeanDefinitions

   ```java
   public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
      //实例化 BeanDefinitionDocumentReader
      BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
     
      //统计之前加载的beandefinition个数
      int countBefore = getRegistry().getBeanDefinitionCount();
     
      //beanDefinition加载
      documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
     
      //返回本次加载的个数
      return getRegistry().getBeanDefinitionCount() - countBefore;
   }
   ```

8. **DefaultBeanDefinitionDocumentReader**#doRegisterBeanDefinitons

   ```java
   protected void doRegisterBeanDefinitions(Element root) {
      // Any nested <beans> elements will cause recursion in this method. In
      // order to propagate and preserve <beans> default-* attributes correctly,
      // keep track of the current (parent) delegate, which may be null. Create
      // the new (child) delegate with a reference to the parent for fallback purposes,
      // then ultimately reset this.delegate back to its original (parent) reference.
      // this behavior emulates a stack of delegates without actually necessitating one.
      BeanDefinitionParserDelegate parent = this.delegate;
      this.delegate = createDelegate(getReaderContext(), root, parent);
   
      if (this.delegate.isDefaultNamespace(root)) {
         //处理profile属性
         String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
         if (StringUtils.hasText(profileSpec)) {
            String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
                  profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
            // We cannot use Profiles.of(...) since profile expressions are not supported
            // in XML config. See SPR-12458 for details.
            if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
               if (logger.isDebugEnabled()) {
                  logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
                        "] not matching: " + getReaderContext().getResource());
               }
               return;
            }
         }
      }
   
      //解析前处理，给子类实现（模版方法，面向继承设计）
      preProcessXml(root);
      //bean元信息解析和bean注册（BeanDefinitionReaderUtils）
      parseBeanDefinitions(root, this.delegate);
      //解析后处理，给子类实现
      postProcessXml(root);
   
      this.delegate = parent;
   }
   ```

9. **DefaultBeanDefinitionDocumentReader**#parseBeanDefinitions

   ```java
   /**
    * Parse the elements at the root level in the document:
    * "import", "alias", "bean".
    * @param root the DOM root element of the document
    */
   protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
      if (delegate.isDefaultNamespace(root)) {
         NodeList nl = root.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
               Element ele = (Element) node;
              
               if (delegate.isDefaultNamespace(ele)) {
                  //默认命名空间解析
                  parseDefaultElement(ele, delegate);
               }
               else {
                  delegate.parseCustomElement(ele);
               }
            }
         }
      }
      else {
         //自定义命名空间解析
         delegate.parseCustomElement(root);
      }
   }
   ```

   

##### 默认标签解析

​	parseDefaultElement(ele, delegate);方法入口

1. **DefaultBeanDefinitionDocumentReader**#parseDefaultElement

   对四种不同标签进行处理：import、alias、bean、beans

   ```java
   private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
      //处理import标签
      if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
         importBeanDefinitionResource(ele);
      }
      //处理alias标签
      else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
         processAliasRegistration(ele);
      }
      //处理bean标签
      else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
         processBeanDefinition(ele, delegate);
      }
      //处理beans标签
      else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
         // recurse 递归
         doRegisterBeanDefinitions(ele);
      }
   }
   ```

2. **DefaultBeanDefinitionDocumentReader**#processBeanDefinition

   对bean标签处理

   1. 委托类BeanDefinitionParseDelegate 进行元信息分析返回BeanDefinitionHolder
   2. 存在自定义标签的情况下，对自定义标签进行处理
   3. BeanDefinitionReaderUtils注册BeanDefinition
   4. 发出响应时间，通知相关监听器，bean已经加载完成

   ```java
   protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
      //委托类进行元信息解析
      BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
      if (bdHolder != null) {
         //自定义标签解析
         bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
         try {
            // Register the final decorated instance.
            //注册BeanDefinition
            BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
         }
         catch (BeanDefinitionStoreException ex) {
            getReaderContext().error("Failed to register bean definition with name '" +
                  bdHolder.getBeanName() + "'", ele, ex);
         }
         // Send registration event.
         getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
      }
   }
   ```

3. **BeanDefinitionParseDelegate**#parseBeanDefinitionElement ，返回BeanDefinition

   元信息解析

   1. 解析ID、name属性

   2. 解析其他属性封装GenericBeanDefinition实例中

   3. 没有指定beanName，按默认规则生成beanName

   4. 信息封装在BeanDefinitionHolder实例中

      ```java
      public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, @Nullable BeanDefinition containingBean) {
         //解析ID、name属性
         String id = ele.getAttribute(ID_ATTRIBUTE);
         String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
      
         //分割alias name属性
         List<String> aliases = new ArrayList<>();
         if (StringUtils.hasLength(nameAttr)) {
            String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
            aliases.addAll(Arrays.asList(nameArr));
         }
      
         String beanName = id;
         if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
            beanName = aliases.remove(0);
            if (logger.isTraceEnabled()) {
               logger.trace("No XML 'id' specified - using '" + beanName +
                     "' as bean name and " + aliases + " as aliases");
            }
         }
      
         if (containingBean == null) {
            checkNameUniqueness(beanName, aliases, ele);
         }
      
         //解析其他属性	
         AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
         if (beanDefinition != null) {
            if (!StringUtils.hasText(beanName)) {
               try {
                  if (containingBean != null) {
                     //按照默认规则生成bean name
                     beanName = BeanDefinitionReaderUtils.generateBeanName(
                           beanDefinition, this.readerContext.getRegistry(), true);
                  }
                  else {
                     beanName = this.readerContext.generateBeanName(beanDefinition);
                     // Register an alias for the plain bean class name, if still possible,
                     // if the generator returned the class name plus a suffix.
                     // This is expected for Spring 1.2/2.0 backwards compatibility.
                     String beanClassName = beanDefinition.getBeanClassName();
                     if (beanClassName != null &&
                           beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() &&
                           !this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
                        aliases.add(beanClassName);
                     }
                  }
                  if (logger.isTraceEnabled()) {
                     logger.trace("Neither XML 'id' nor 'name' specified - " +
                           "using generated bean name [" + beanName + "]");
                  }
               }
               catch (Exception ex) {
                  error(ex.getMessage(), ele);
                  return null;
               }
            }
            String[] aliasesArray = StringUtils.toStringArray(aliases);
            return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
         }
      
         return null;
      }
      ```

4. **BeanDefinitionParseDelegate**#parseBeanDefinitionElement ，返回AbstractBeanDefinition

   解析其他属性

   ```java
   public AbstractBeanDefinition parseBeanDefinitionElement(
         Element ele, String beanName, @Nullable BeanDefinition containingBean) {
   
      this.parseState.push(new BeanEntry(beanName));
   
      //解析class属性
      String className = null;
      if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
         className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
      }
      //parent
      String parent = null;
      if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
         parent = ele.getAttribute(PARENT_ATTRIBUTE);
      }
   
      try {
         //创建GenericBeanDefinition承载属性
         AbstractBeanDefinition bd = createBeanDefinition(className, parent);
   			//解析默认bean的各种属性
         parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
         //解析description
         bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));
   			
         //解析元数据
         parseMetaElements(ele, bd);
         //解析lock-method属性
         parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
         //解析replace-method属性
         parseReplacedMethodSubElements(ele, bd.getMethodOverrides());
   
         //解析构造函数参数
         parseConstructorArgElements(ele, bd);
         //解析property子元素
         parsePropertyElements(ele, bd);
         //解析qualifier子元素
         parseQualifierElements(ele, bd);
   
         bd.setResource(this.readerContext.getResource());
         bd.setSource(extractSource(ele));
   
         return bd;
      }
      catch (ClassNotFoundException ex) {
         error("Bean class [" + className + "] not found", ele, ex);
      }
      catch (NoClassDefFoundError err) {
         error("Class that bean class [" + className + "] depends on not found", ele, err);
      }
      catch (Throwable ex) {
         error("Unexpected failure during bean definition parsing", ele, ex);
      }
      finally {
         this.parseState.pop();
      }
   
      return null;
   }
   ```

5. BeanDefinitionReaderUtils.registerBeanDefinition 

   BeanDefinition注册

   ```java
   public static void registerBeanDefinition(
         BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
         throws BeanDefinitionStoreException {
   
      // Register bean definition under primary name.
      String beanName = definitionHolder.getBeanName();
      //beanName 作为唯一表示注册
      registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
   
      // Register aliases for bean name, if any.
      String[] aliases = definitionHolder.getAliases();
      //注册所有别名
      if (aliases != null) {
         for (String alias : aliases) {
            registry.registerAlias(beanName, alias);
         }
      }
   }
   ```

   1. beanName注册BeanDefinition（DefaultListableBeanFactory#registerBeanDefinition）

      ```java
      public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
      			throws BeanDefinitionStoreException {
      
      		Assert.hasText(beanName, "Bean name must not be empty");
      		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
      
      		if (beanDefinition instanceof AbstractBeanDefinition) {
      			try {
      				//注册前的最后一次校验 ，这里的校验不同于之前的 XML 文件校验，
      				//主要是对于 AbstractBeanDefinition 属性中的 methodOverrides 校验，
      				//校验methodOverrides是否与工厂方法并存或者methodOverrides对应的方法根本不存在
      				((AbstractBeanDefinition) beanDefinition).validate();
      			}
      			catch (BeanDefinitionValidationException ex) {
      				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
      						"Validation of bean definition failed", ex);
      			}
      		}
      
      		BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
        	//处理已经注册的beanDefinition
      		if (existingDefinition != null) {
            //bean不允许覆盖，抛出异常
            //setAllowBeanDefinitionOverriding 可修改该值
      			if (!isAllowBeanDefinitionOverriding()) {
      				throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
      			}
      			else if (existingDefinition.getRole() < beanDefinition.getRole()) {
      				// e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
      				if (logger.isInfoEnabled()) {
      					logger.info("Overriding user-defined bean definition for bean '" + beanName +
      							"' with a framework-generated bean definition: replacing [" +
      							existingDefinition + "] with [" + beanDefinition + "]");
      				}
      			}
      			else if (!beanDefinition.equals(existingDefinition)) {
      				if (logger.isDebugEnabled()) {
      					logger.debug("Overriding bean definition for bean '" + beanName +
      							"' with a different definition: replacing [" + existingDefinition +
      							"] with [" + beanDefinition + "]");
      				}
      			}
      			else {
      				if (logger.isTraceEnabled()) {
      					logger.trace("Overriding bean definition for bean '" + beanName +
      							"' with an equivalent definition: replacing [" + existingDefinition +
      							"] with [" + beanDefinition + "]");
      				}
      			}
            //注册beanDefinition
      			this.beanDefinitionMap.put(beanName, beanDefinition);
      		}
      		else {
            // 如果beanDefinition已经被标记为创建(为了解决单例bean的循环依赖问题)
      			if (hasBeanCreationStarted()) {
      				// Cannot modify startup-time collection elements anymore (for stable iteration)
      				synchronized (this.beanDefinitionMap) {
      					this.beanDefinitionMap.put(beanName, beanDefinition);
                // 创建List<String>并将缓存的beanDefinitionNames和新解析的beanName加入集合
      					List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
      					updatedDefinitions.addAll(this.beanDefinitionNames);
      					updatedDefinitions.add(beanName);
      					this.beanDefinitionNames = updatedDefinitions;
                
                // 如果manualSingletonNames中包含新注册的beanName，移除新注册的beanDefinition
      					removeManualSingletonName(beanName);
      				}
      			}
      			else {
      				// Still in startup registration phase
      				this.beanDefinitionMap.put(beanName, beanDefinition);
      				this.beanDefinitionNames.add(beanName);
      				removeManualSingletonName(beanName);
      			}
      			this.frozenBeanDefinitionNames = null;
      		}
      
          // 重置BeanDefinition，
          // 当前注册的bean的定义已经在beanDefinitionMap缓存中存在，
          // 或者其实例已经存在于单例bean的缓存中
      		if (existingDefinition != null || containsSingleton(beanName)) {
      			resetBeanDefinition(beanName);
      		}
      	}
      ```

      

   2. allies注册beanDefinition（SimpleAliasRegistry#registry）

      ```java
      @Override
      public void registerAlias(String name, String alias) {
          Assert.hasText(name, "'name' must not be empty");
          Assert.hasText(alias, "'alias' must not be empty");
          synchronized (this.aliasMap) {
              // 如果beanName与别名相同
              if (alias.equals(name)) {
                  //移除别名
                  this.aliasMap.remove(alias);
                  if (logger.isDebugEnabled()) {
                      logger.debug("Alias definition '" + alias + "' ignored since it points to same name");
                  }
              }
              else {
                  String registeredName = this.aliasMap.get(alias);
                  //如果别名已经在缓存中存在
                  if (registeredName != null) {
                      //缓存中的别名和beanName(注意:不是别名)相同,不做任何操作,不需要再次注册
                      if (registeredName.equals(name)) {
                          // An existing alias - no need to re-register
                          return;
                      }
                      //缓存中存在别名,且不允许覆盖,抛出异常
                      if (!allowAliasOverriding()) {
                          throw new IllegalStateException("Cannot define alias '" + alias + "' for name '" +
                                  name + "': It is already registered for name '" + registeredName + "'.");
                      }
                  }
                  //检查给定名称是否已指向另一个方向的别名作为别名,预先捕获循环引用并抛出异常
                  checkForAliasCircle(name, alias);
                  //缓存别名
                  this.aliasMap.put(alias, name);
              }
          }
      }
      ```

      

##### 自定义标签解析



