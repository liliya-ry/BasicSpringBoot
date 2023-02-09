package SpringBoot.application;

import SpringBoot.servlet.*;
import SpringContainer.SpringContainer;
import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.*;

public class WebAdapter implements Adapter {
    private static final String DEFAULT_TOMCAT_PORT = "8080";
    private static final String DEFAULT_CONTEXT_PATH = "";

    private final Tomcat tomcat = new Tomcat();

    public void configure(ApplicationContext appContext) throws Exception {
        ControllersDispatcher controllerDispatcher = new ControllersDispatcher(appContext);
        appContext.springContainer.getBean(ControllersDispatcher.class, controllerDispatcher);
        setUpTomcat(appContext);
        tomcat.start();
    }

    private void setUpTomcat(ApplicationContext appContext) throws Exception {
        String portStr = appContext.appProperties.getProperty("server.port", DEFAULT_TOMCAT_PORT);
        int port = Integer.parseInt(portStr);
        tomcat.setPort(port);
        tomcat.getConnector();

        String contextPath = appContext.appProperties.getProperty("server.servlet.context-path", DEFAULT_CONTEXT_PATH);
        Context context = tomcat.addContext(contextPath, null);

        addDispatcherServlet(tomcat, contextPath, context, appContext.springContainer);
        addInterceptorFilter(context, appContext.springContainer);
    }

    private void addInterceptorFilter(Context context, SpringContainer springContainer) throws Exception {
        String filterName = InterceptorFilter.class.getSimpleName();
        FilterDef filterDef = new FilterDef();
        filterDef.setFilter(springContainer.getBean(InterceptorFilter.class));
        filterDef.setFilterName(filterName);
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(filterName);
        filterMap.addURLPattern("/*");
        context.addFilterMap(filterMap);
    }

    private void addDispatcherServlet(Tomcat tomcat, String contextPath, Context context, SpringContainer springContainer) throws Exception {
        String servletName = DispatcherServlet.class.getSimpleName();
        HttpServlet servlet = springContainer.getBean(DispatcherServlet.class);
        tomcat.addServlet(contextPath, servletName, servlet);
        context.addServletMappingDecoded("/*", servletName);
    }
}
