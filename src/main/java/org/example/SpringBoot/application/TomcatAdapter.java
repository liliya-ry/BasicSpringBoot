package org.example.SpringBoot.application;

import static org.example.SpringBoot.application.SpringApplication.springAdapter;

import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.example.SpringBoot.servlet.DispatcherServlet;
import org.example.SpringBoot.servlet.InterceptorFilter;

import java.util.Properties;

public class TomcatAdapter {
    private static final String DEFAULT_TOMCAT_PORT = "8080";
    private static final String DEFAULT_CONTEXT_PATH = "";

    private final Tomcat tomcat;

    TomcatAdapter(Properties appProperties) throws Exception {
        tomcat = new Tomcat();
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
        filterDef.setFilter(springAdapter.getSpringContainer().getBean(InterceptorFilter.class));
        filterDef.setFilterName(filterName);
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(filterName);
        filterMap.addURLPattern("/*");
        context.addFilterMap(filterMap);
    }

    private static void addDispatcherServlet(Tomcat tomcat, String contextPath, Context context) throws Exception {
        String servletName = DispatcherServlet.class.getSimpleName();
        HttpServlet servlet = springAdapter.getSpringContainer().getBean(DispatcherServlet.class);
        tomcat.addServlet(contextPath, servletName, servlet);
        context.addServletMappingDecoded("/*", servletName);
    }

    void startServer() throws LifecycleException {
        tomcat.start();
    }
}
