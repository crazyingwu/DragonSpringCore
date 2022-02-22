package com.dragonSpringCore.aop;

import com.dragonSpringCore.aop.aspect.Advice;
import com.dragonSpringCore.aop.support.AdviceSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @ClassName JdkDynamicAopProxy
 * @Description 生成代理类的工具类
 **/

public class JdkDynamicAopProxy implements InvocationHandler {

    private AdviceSupport support;

    public JdkDynamicAopProxy(AdviceSupport support) {
        this.support = support;
    }

    /**
     * 功能描述: 返回一个代理对象
     * @param
     * @return: java.lang.Object
     **/
    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.support.getTargetClass().getInterfaces(), this);
    }

    /**
     * 功能描述: 重写invoke
     * @param proxy
     * @param method
     * @param args
     * @return: java.lang.Object
     **/
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Map<String, Advice> advices = support.getAdvices(method, support.getTargetClass());

        Object result = null;
        try {
            //调用前置通知
            invokeAdvice(advices.get("before"));

            //执行原生目标方法
            result = method.invoke(support.getTarget(), args);

            //调用后置通知
            invokeAdvice(advices.get("after"));
        } catch (Exception e) {
            //调用异常通知
            invokeAdvice(advices.get("afterThrowing"));
            throw e;
        }

        return result;
    }

    /**
     * 功能描述: 执行切面方法
     * @param advice
     * @return: void
     **/
    private void invokeAdvice(Advice advice) {
        try {
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
