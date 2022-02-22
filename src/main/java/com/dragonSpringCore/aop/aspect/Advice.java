package com.dragonSpringCore.aop.aspect;

import java.lang.reflect.Method;

public class Advice {

    /**
     * 通知对象
     */
    private Object aspect;

    /**
     * 通知方法
     */
    private Method adviceMethod;

    /**
     * 拦截指定异常名
     */
    private String throwingName;

    public Advice(Object aspect, Method method) {
        this.aspect = aspect;
        this.adviceMethod = method;
    }

    public Object getAspect() {
        return aspect;
    }

    public Method getAdviceMethod() {
        return adviceMethod;
    }

    public String getThrowingName() {
        return throwingName;
    }

    public void setThrowingName(String throwingName) {
        this.throwingName = throwingName;
    }

}
