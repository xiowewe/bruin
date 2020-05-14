package com.bruin.expansion.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

/**
 * @description: 自定义SpringApplicationRunListener
 * @author: xiongwenwen   2020/5/12 16:49
 */
//@Component SpringApplicationRunListener在启动监听器是加载，spring.factories即可
public class DemoSpringApplicationRunListenner implements SpringApplicationRunListener {

    /**必须提供参数SpringApplication application, String[] args的有参构造器*/
    public DemoSpringApplicationRunListenner(SpringApplication application, String[] args) {
    }

    @Override
    public void starting() {
        System.out.println("在run()方法开始执行时");
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        System.out.println("environment构建完成，ApplicationContext创建之前");
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        System.out.println("当ApplicationContext构建完成时");
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        System.out.println("ApplicationContext完成加载，但没有被刷新");
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        System.out.println("ApplicationContext刷新并启动后，CommandLineRunners和ApplicationRunner未被调用前");
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        System.out.println("在run()方法执行完成前");
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        System.out.println("当应用运行出错时");
    }
}
