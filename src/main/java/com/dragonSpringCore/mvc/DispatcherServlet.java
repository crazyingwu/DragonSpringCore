package com.dragonSpringCore.mvc;


import com.dragonSpringCore.annotation.*;
import com.dragonSpringCore.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DispatcherServlet extends HttpServlet {

    private ApplicationContext applicationContext;

    private List<HandlerMapping> handlerMappings = new ArrayList<>();

    private Map<HandlerMapping, HandlerAdapter> handlerAdapterMap = new HashMap<>();

    private List<ViewResolver> viewResolvers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            try {
                processDispatchResult(req, resp, new ModelAndView("500", new HashMap<String, Object>() {
                    {
                        put("detail", "Exception Code : 500\nException Detail:");
                        put("message", e.getCause().getCause().getMessage() == null ? "报错了" : e.getCause().getCause().getMessage());
                        put("stackTrace", Arrays.toString(e.getStackTrace()));
                    }
                }));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 功能描述: 处理请求
     *
     * @param req
     * @param resp
     * @return: void
     **/
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1.根据url拿到对应的HandlerMapping
        HandlerMapping handlerMapping = getHandler(req);

        //如果没有对应的HandlerMapping,返回404页面
        if (handlerMapping == null) {
            processDispatchResult(req, resp, new ModelAndView("404"));
            return;
        }

        //2.根据HandlerMapping拿到一个HandlerAdapter
        HandlerAdapter ha = getHandlerAdapter(handlerMapping);

        //3.根据HandlerAdapter拿到一个ModelAndView
        ModelAndView mv = ha.handle(req, resp, handlerMapping);

        //4.根据ModelAndView决定选择哪个ViewResolver进行模板解析渲染
        processDispatchResult(req, resp, mv);

    }

    /**
     * 功能描述: 初始化策略
     *
     * @param config
     * @return: void
     **/
    @Override
    public void init(ServletConfig config) {
        applicationContext = new ApplicationContext(config.getInitParameter("contextConfigLocation"));
        //初始化核心组件
        initStrategies(applicationContext);
        //初始化完成
        System.err.println("Spring dragonSpringCore is init!");
    }

    /**
     * 功能描述: 进行模板解析渲染
     *
     * @param req
     * @param resp
     * @param mv
     * @return: void
     **/
    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView mv) throws Exception {
        //返回json
        if (mv == null) {
            return;
        }

        //返回页面
        for (ViewResolver viewResolver : viewResolvers) {
            View view = viewResolver.resolveViewName(mv.getViewName());
            view.render(mv.getModel(), req, resp);
            return;
        }
    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping mappedHandler) {
        return handlerAdapterMap.get(mappedHandler);
    }

    /**
     * 功能描述: 获取HandlerMapping
     *
     * @param req
     * @return: com.dragonSpringCore.mvc.HandlerMapping
     **/
    private HandlerMapping getHandler(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        String url = req.getRequestURI().replaceAll(contextPath, "");
        return handlerMappings.stream()
                .filter(v -> v.getPattern().matcher(url).matches())
                .findFirst()
                .orElse(null);
    }


    /**
     * 功能描述: 初始化核心组件 在Spring中有九大核心组件，这里只实现三种
     *
     * @param context
     * @return: void
     **/
    protected void initStrategies(ApplicationContext context) {
        //多文件上传组件
        //initMultipartResolver(context);
        //初始化本地语言环境
        //initLocaleResolver(context);
        //初始化模板处理器
        //initThemeResolver(context);
        //初始化请求分发处理器
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
        //初始化异常拦截器
        //initHandlerExceptionResolvers(context);
        //初始化视图预处理器
        //initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
        //缓存管理器(值栈)
        //initFlashMapManager(context);
    }

    /**
     * 功能描述: 初始化视图转换器
     *
     * @param context
     * @return: void
     **/
    private void initViewResolvers(ApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String filePath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(filePath);
        Stream.of(templateRootDir.listFiles()).forEach(v -> viewResolvers.add(new ViewResolver(templateRoot)));
    }

    /**
     * 功能描述: 初始化参数适配器
     *
     * @param context
     * @return: void
     **/
    private void initHandlerAdapters(ApplicationContext context) {
        handlerMappings.forEach(v -> handlerAdapterMap.put(v, new HandlerAdapter()));
    }

    /**
     * 功能描述: 初始化请求分发处理器
     *
     * @param context
     * @return: void
     **/
    private void initHandlerMappings(ApplicationContext context) {
        if (applicationContext.getBeanDefinitionCount() == 0) {
            return;
        }

        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }
            String baseUrl = "";
            //如果类上面加了RequestMapping注解,则需要拿到url进行拼接
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                baseUrl = annotation.value();
            }

            //获取所有public修饰的方法
            Method[] methods = clazz.getMethods();
            //过滤拿到所有RequestMapping注解的方法,put到handlerMapping中
            String finalBaseUrl = baseUrl;
            Stream.of(methods)
                    .filter(m -> m.isAnnotationPresent(RequestMapping.class))
                    .forEach(m -> {
                        RequestMapping annotation = m.getAnnotation(RequestMapping.class);
                        String regex = (finalBaseUrl + annotation.value())
                                .replaceAll("/+", "/")
                                .replaceAll("\\*", ".*");
                        Pattern pattern = Pattern.compile(regex);
                        handlerMappings.add(new HandlerMapping(pattern, instance, m));
                    });
        }

    }
}
