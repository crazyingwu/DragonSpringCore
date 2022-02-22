package com.dragonSpringCore.context;

import com.dragonSpringCore.annotation.Autowired;
import com.dragonSpringCore.annotation.Controller;
import com.dragonSpringCore.annotation.Service;
import com.dragonSpringCore.aop.JdkDynamicAopProxy;
import com.dragonSpringCore.aop.config.AopConfig;
import com.dragonSpringCore.aop.support.AdviceSupport;
import com.dragonSpringCore.beans.BeanWrapper;
import com.dragonSpringCore.beans.config.BeanDefinition;
import com.dragonSpringCore.beans.support.BeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @ClassName ApplicationContext
 * @Description Spring顶层容器封装
 **/

public class ApplicationContext {

    private String[] configLocations;

    /**
     * 解析配置文件的工具类
     */
    private BeanDefinitionReader beanDefinitionReader;

    /**
     * BeanName与className的缓存
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    /**
     * IOC容器
     */
    private Map<String, BeanWrapper> factoryBeanInstanceCache = new HashMap<>();

    /**
     * 原生对象的缓存
     */
    private Map<String, Object> factoryBeanObjectCache = new HashMap<>();

   /**
    * 功能描述: 初始化ApplicationContext
    * @param configLocations
    * @return:
    **/
    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;

        try {
            //1.读取配置文件并解析BeanDefinition对象
            beanDefinitionReader = new BeanDefinitionReader(configLocations);
            List<BeanDefinition> beanDefinitionList = beanDefinitionReader.loadBeanDefinitions();

            //2.将解析后的BeanDefinition对象注册到beanDefinitionMap中
            doRegisterBeanDefinition(beanDefinitionList);

            //3.触发创建对象的动作,调用getBean()方法(Spring默认是延时加载)
            doCreateBean();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能描述: 对容器的初始化
     * @param
     * @return: void
     **/
    private void doCreateBean() {
        beanDefinitionMap.forEach((k, v) -> getBean(k));
    }

    private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitionList) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitionList) {
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    /**
     * 功能描述: 真正触发IoC和DI的动作  1.创建Bean  2.依赖注入
     * @param beanName
     * @return: java.lang.Object
     **/
    public Object getBean(String beanName) {
        //============ 创建实例 ============

        //1.获取配置信息,只要拿到beanDefinition对象即可
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        //用反射创建实例  这个实例有可能是代理对象 也有可能是原生对象   封装成BeanWrapper统一处理
        Object instance = instantiateBean(beanName, beanDefinition);
        BeanWrapper beanWrapper = new BeanWrapper(instance);

        factoryBeanInstanceCache.put(beanName, beanWrapper);

        //============ 依赖注入 ============
        populateBean(beanName, beanDefinition, beanWrapper);

        return beanWrapper.getWrapperInstance();
    }

    private AdviceSupport instantiateAopConfig(BeanDefinition beanDefinition) {
        AopConfig AopConfig = new AopConfig();

        AopConfig.setPointCut(beanDefinitionReader.getConfig().getProperty("pointCut"));
        AopConfig.setAspectClass(beanDefinitionReader.getConfig().getProperty("aspectClass"));
        AopConfig.setAspectBefore(beanDefinitionReader.getConfig().getProperty("aspectBefore"));
        AopConfig.setAspectAfter(beanDefinitionReader.getConfig().getProperty("aspectAfter"));
        AopConfig.setAspectAfterThrowing(beanDefinitionReader.getConfig().getProperty("aspectAfterThrowing"));
        AopConfig.setAspectAfterThrowingName(beanDefinitionReader.getConfig().getProperty("aspectAfterThrowingName"));

        return new AdviceSupport(AopConfig);

    }

    /**
     * 功能描述: 依赖注入
     * @param beanName
     * @param beanDefinition
     * @param beanWrapper
     * @return: void
     **/
    private void populateBean(String beanName, BeanDefinition beanDefinition, BeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrapperInstance();
        Class<?> clazz = beanWrapper.getWrapperClass();

        //只有加了注解的类才需要依赖注入
        if (!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))) {
            return;
        }

        //拿到bean所有的字段 包括private、public、protected、default
        for (Field field : clazz.getDeclaredFields()) {

            //如果没加Autowired注解的属性则直接跳过
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            Autowired annotation = field.getAnnotation(Autowired.class);
            String autowiredBeanName = annotation.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }
            //强制访问
            field.setAccessible(true);
            try {
                if (factoryBeanInstanceCache.get(autowiredBeanName) == null) { continue; }
                //赋值
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 功能描述: 反射实例化对象
     * @param beanName
     * @param beanDefinition
     * @return: java.lang.Object
     **/
    private Object instantiateBean(String beanName, BeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();

        Object instance = null;
        try {
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();

            /**
             *  ===========接入AOP begin===========
             */
            AdviceSupport support = instantiateAopConfig(beanDefinition);
            support.setTargetClass(clazz);
            support.setTarget(instance);
            //如果需要代理  则用代理对象覆盖目标对象
            if (support.pointCutMatch()) {
                instance = new JdkDynamicAopProxy(support).getProxy();
            }
            /**
             * ===========接入AOP end===========
             */

            factoryBeanObjectCache.put(beanName, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Object getBean(Class beanClass) {
        return getBean(beanClass.getName());
    }

    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return beanDefinitionReader.getConfig();
    }
}
