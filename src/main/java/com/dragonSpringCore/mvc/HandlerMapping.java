package com.dragonSpringCore.mvc;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @ClassName HandlerMapping
 **/

public class HandlerMapping {

    /**
     * 请求路径校验
     */
    private Pattern pattern;

    /**
     * 实例对象
     */
    private Object controller;

    /**
     * 处理方法
     */
    private Method method;

    public HandlerMapping(Pattern pattern, Object instance, Method method) {
        this.pattern = pattern;
        this.controller = instance;
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
