package com.dragonSpringCore.aop.support;

import com.dragonSpringCore.aop.aspect.Advice;
import com.dragonSpringCore.aop.config.AopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AdviceSupport {

    /**
     * 原生对象class
     */
    private Class targetClass;

    /**
     * 原生对象实例
     */
    private Object target;

    /**
     * 配置信息
     */
    private AopConfig aopConfig;

    /**
     * 匹配正则
     */
    private Pattern pointCutClassPattern;

    /**
     * 方法对应的所有通知
     */
    private Map<Method, Map<String, Advice>> methodCache;

    public AdviceSupport(AopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    /**
     * 功能描述: 正则匹配结果
     * @param
     * @return: boolean
     **/
    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(targetClass.getName()).matches();
    }

    /**
     * 功能描述: 获取通知方法
     * @param method       目标类方法
     * @param targetClass  目标类class
     * @return: java.util.Map<java.lang.String,com.com.dragonSpringCore.aop.aspect.Advice>
     **/
    public Map<String, Advice> getAdvices(Method method, Class targetClass) throws Exception {
        Map<String, Advice> aspectMethodMap = methodCache.get(method);
        if (aspectMethodMap == null) {
            Method m = this.targetClass.getMethod(method.getName(), method.getParameterTypes());
            aspectMethodMap = methodCache.get(m);
            methodCache.put(m, aspectMethodMap);
        }
        return aspectMethodMap;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    /**
     * 功能描述: 解析配置  pointCut
     * @param
     * @return: void
     **/
    private void parse() {
        String pointCut = aopConfig.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        //public .*.com.wqfrw.service..*impl..*(.*)
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        this.pointCutClassPattern = Pattern.compile(pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));

        methodCache = new HashMap<>();
        //匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);

        //1.对回调通知进行缓存
        Map<String, Method> aspectMethods = new HashMap<>();
        try {
            //拿到切面类的class  com.wqfrw.aspect.LogAspect
            Class<?> aspectClass = Class.forName(this.aopConfig.getAspectClass());
            //将切面类的通知方法缓存到aspectMethods
            Stream.of(aspectClass.getMethods()).forEach(v -> aspectMethods.put(v.getName(), v));

            //2.扫描目标类的方法，去循环匹配
            for (Method method : targetClass.getMethods()) {
                String methodString = method.toString();
                //如果目标方法有抛出异常  则截取
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                /**
                 * 匹配目标类方法   如果匹配上,就将缓存好的通知与它建立联系  如果没匹配上,则忽略
                 */
                Matcher matcher = pointCutPattern.matcher(methodString);
                if (matcher.matches()) {
                    Map<String, Advice> adviceMap = new HashMap<>();
                    //前置通知
                    if (!(null == aopConfig.getAspectBefore() || "".equals(aopConfig.getAspectBefore()))) {
                        adviceMap.put("before", new Advice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectBefore())));
                    }

                    //后置通知
                    if (!(null == aopConfig.getAspectAfter() || "".equals(aopConfig.getAspectAfter()))) {
                        adviceMap.put("after", new Advice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectAfter())));
                    }

                    //异常通知
                    if (!(null == aopConfig.getAspectAfterThrowing() || "".equals(aopConfig.getAspectAfterThrowing()))) {
                        Advice advice = new Advice(aspectClass.newInstance(), aspectMethods.get(aopConfig.getAspectAfterThrowing()));
                        advice.setThrowingName(aopConfig.getAspectAfterThrowingName());
                        adviceMap.put("afterThrowing", advice);
                    }
                    //建立关联
                    methodCache.put(method, adviceMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Class getTargetClass() {
        return targetClass;
    }
}
