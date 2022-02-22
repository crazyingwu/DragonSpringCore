package com.dragonSpringCore.beans.config;

/**
 * @ClassName BeanDefinition
 * @Description 存储配置信息
 **/
public class BeanDefinition {

    private String factoryBeanName;

    private String beanClassName;

    public BeanDefinition(String factoryBeanName, String beanClassName) {
        this.factoryBeanName = factoryBeanName;
        this.beanClassName = beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
