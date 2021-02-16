package com.lagou.edu;

import com.lagou.edu.context.MyWebApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author wuwenbin
 */
public class MyWebApplication implements ServletContextListener {

    private static final String CONFIG_CLASS_KEY = "configClass";
    public static final String GLOBAL_APPLICATION_CONTEXT_KEY = "applicationContext";

    private MyWebApplicationContext applicationContext;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        //根据 web.xml 中的配置信息获取配置类对象 MyWebConfig.class
        //需要说明的一点，此处作业中我没有考虑依赖注入的顺序功能即@Order 的顺序以及@DependsOn 的功能
        Class<?> configClass = getConfigClass(servletContextEvent);
        try {
            this.applicationContext = new MyWebApplicationContext(configClass);
            servletContextEvent.getServletContext().setAttribute(GLOBAL_APPLICATION_CONTEXT_KEY, applicationContext);
        } catch (Exception e) {
            e.printStackTrace();
            //结束程序
            System.exit(-1);
        }
    }


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //简单的销毁
        this.applicationContext.clearAllBeans();
        this.applicationContext = null;
        servletContextEvent.getServletContext().removeAttribute(GLOBAL_APPLICATION_CONTEXT_KEY);
        System.gc();
    }

    //========================= 私有方法 ==========================


    /**
     * 获取配置类对象
     *
     * @return
     */
    private Class<?> getConfigClass(ServletContextEvent servletContextEvent) {
        String configClass = servletContextEvent.getServletContext().getInitParameter(CONFIG_CLASS_KEY);
        try {
            return Class.forName(configClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


}
