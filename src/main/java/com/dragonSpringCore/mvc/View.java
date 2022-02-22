package com.dragonSpringCore.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName View
 * @Description 页面的封装
 **/

public class View {

    /**
     * 页面文件
     */
    private File viewFile;

    public View(File templateFile) {
        this.viewFile = templateFile;
    }

    /**
     * 功能描述: 对页面内容进行渲染
     * @param model
     * @param req
     * @param resp
     * @return: void
     **/
    public void render(Map<String, ?> model, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        StringBuilder sb = new StringBuilder();
        //只读模式 读取文件
        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");

        String line = null;
        while ((line = ra.readLine()) != null) {
            line = new String(line.getBytes("ISO-8859-1"), "utf-8");

            //%{name}
            Pattern pattern = Pattern.compile("%\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);

            while (matcher.find()) {
                String paramName = matcher.group();

                paramName = paramName.replaceAll("%\\{|\\}", "");
                Object paramValue = model.get(paramName);
                line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                matcher = pattern.matcher(line);
            }
            sb.append(line);
        }

        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(sb.toString());
    }

    /**
     * 功能描述: 处理特殊字符
     * @param str
     * @return: java.lang.String
     **/
    private String makeStringForRegExp(String str){
        return str.replace("\\","\\\\").replace("*","\\*")
                .replace("+","\\+").replace("|","\\|")
                .replace("{","\\{").replace("}","\\}")
                .replace("(","\\(").replace(")","\\)")
                .replace("^","\\^").replace("$","\\$")
                .replace("[","\\[").replace("]","\\]")
                .replace("?","\\?").replace(",","\\,")
                .replace(".","\\.").replace("&","\\&");

    }

}
