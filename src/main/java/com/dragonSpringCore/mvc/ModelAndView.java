package com.dragonSpringCore.mvc;

import java.util.Map;

/**
 * @ClassName ModelAndView
 * @Description 视图解析器
 **/

public class ModelAndView {

    /**
     * 返回页面文件名
     */
    private String viewName;

    /**
     * 返回数据
     */
    private Map<String, ?> model;


    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public ModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

}
