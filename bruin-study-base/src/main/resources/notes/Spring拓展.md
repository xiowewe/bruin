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





