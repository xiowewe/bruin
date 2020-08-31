#### 示例

#### 自定义标签

- 注册AnnotationAwareAspectJAutoProxyCreator
  - 注册或者升级AnnotationAwareAspectJAutoProxyCreator
  - 处理proxy-target-class（代理类型）、expose-proxy属性

#### 创建代理

实现BeanPostProcessor的postProcessAfterInitialization接口：

- 获取增加方法或者增强器
- 根据获取的增强进行代理

##### 获取增强

​	获取所有BeanName