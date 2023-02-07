package org.example.SpringBoot.application;

import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.*;
import org.example.SpringBoot.servlet.*;
import org.example.SpringContainer.ApplicationContext;

import java.util.Properties;

public class TomcatAdapter {
    private static final String DEFAULT_TOMCAT_PORT = "8080";
    private static final String DEFAULT_CONTEXT_PATH = "";

    private final Tomcat tomcat = new Tomcat();
    private final ApplicationContext appContext;

    TomcatAdapter(Properties appProperties, ApplicationContext appContext) throws Exception {
        this.appContext = appContext;
        setUpTomcat(appProperties);
    }

    private void setUpTomcat(Properties appProperties) throws Exception {
        String portStr = appProperties.getProperty("server.port", DEFAULT_TOMCAT_PORT);
        int port = Integer.parseInt(portStr);
        tomcat.setPort(port);
        tomcat.getConnector();

        String contextPath = appProperties.getProperty("server.servlet.context-path", DEFAULT_CONTEXT_PATH);
        Context context = tomcat.addContext(contextPath, null);

        addDispatcherServlet(tomcat, contextPath, context);
        addInterceptorFilter(context);
    }

    private void addInterceptorFilter(Context context) throws Exception {
        String filterName = InterceptorFilter.class.getSimpleName();
        FilterDef filterDef = new FilterDef();
        filterDef.setFilter(appContext.getBean(InterceptorFilter.class));
        filterDef.setFilterName(filterName);
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(filterName);
        filterMap.addURLPattern("/*");
        context.addFilterMap(filterMap);
    }

    private void addDispatcherServlet(Tomcat tomcat, String contextPath, Context context) throws Exception {
        String servletName = DispatcherServlet.class.getSimpleName();
        HttpServlet servlet = appContext.getBean(DispatcherServlet.class);
        tomcat.addServlet(contextPath, servletName, servlet);
        context.addServletMappingDecoded("/*", servletName);
    }

    void startServer() throws LifecycleException {
        tomcat.start();
    }
}
