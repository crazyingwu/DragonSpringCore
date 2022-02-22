package com.dragonSpringCore.mvc;

import java.io.File;

/**
 * @ClassName ViewResolver
 * @Description 模板引擎
 **/

public class ViewResolver {

    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateRootDir;

    public ViewResolver(String templateRoot) {
        String filePath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.templateRootDir = new File(filePath);
    }

    public View resolveViewName(String viewName) {
        if (viewName == null || "".equals(viewName.trim())) {
            return null;
        }
        //格式化页面后缀
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : viewName + DEFAULT_TEMPLATE_SUFFIX;
        //获取页面文件
        File templateFile = new File(templateRootDir.getPath() + File.separator + viewName);
        return new View(templateFile);
    }
}
