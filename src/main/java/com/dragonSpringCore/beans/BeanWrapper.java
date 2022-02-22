package com.dragonSpringCore.beans;

/**
 * @ClassName BeanWrapper
 * @Description 原生对象和代理对象的包装类
 **/

public class BeanWrapper {

    /**
     * 实例对象
     */
    private Object wrapperInstance;

    /**
     * 对象的类型
     */
    private Class wrapperClass;


    public BeanWrapper(Object instance) {
        this.wrapperClass = instance.getClass();
        this.wrapperInstance = instance;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public void setWrapperInstance(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }

    public Class getWrapperClass() {
        return wrapperClass;
    }

    public void setWrapperClass(Class wrapperClass) {
        this.wrapperClass = wrapperClass;
    }
}
