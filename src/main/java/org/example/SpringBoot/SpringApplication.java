package org.example.SpringBoot;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.example.SpringContainer.Container;
import org.example.SpringContainer.annotations.beans.*;

import java.io.*;
import java.lang.annotation.*;
import java.util.*;

public class SpringApplication {
    private static final String DEFAULT_TOMCAT_PORT = "8080";
    private static final String DEFAULT_CONTEXT_PATH = "";
    private static final String APP_PROPERTIES_FILE_NAME = "src/main/java/%s/resources/application.properties";
    private static final Properties appProperties = new Properties();
    private static final Set<Class<?>> BEAN_TYPES = Set.of(Component.class);
    static final Container CONTAINER = new Container();
    static List<Class<?>> controllers = new ArrayList<>();

    public static void run(Class<?> configurationClass, String[] arguments) throws Exception {
        initContext(configurationClass);
        loadProperties(configurationClass.getPackageName());
        startTomcat();
    }

    private static void startTomcat() throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        String portStr = appProperties.getProperty("server.port", DEFAULT_TOMCAT_PORT);
        int port = Integer.parseInt(portStr);
        tomcat.setPort(port);
        tomcat.getConnector();

        String contextPath = appProperties.getProperty("server.servlet.context-path", DEFAULT_CONTEXT_PATH);
        Context context = tomcat.addContext(contextPath, null);

        String servletName = DispatcherServlet.class.getSimpleName();
        String servletClass = DispatcherServlet.class.toString();
        tomcat.addServlet(contextPath, servletName, servletClass);
        context.addServletMappingDecoded("/*", servletName);

        tomcat.start();
        tomcat.getServer().await();
    }

    private static void initContext(Class<?> configurationClass) throws Exception {
        List<Class<?>> classesList = new ArrayList<>();
        findAllClasses(configurationClass.getPackageName(), classesList);
        for (Class<?> clazz : classesList) {
            Annotation[] annotations = clazz.getDeclaredAnnotations();
            for (Annotation a : annotations) {
                if (BEAN_TYPES.contains(a.getClass())) {
                    CONTAINER.getInstance(a.getClass());
                    continue;
                }

                if ((a instanceof RestController)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces.length == 1)
                        clazz = interfaces[0];
                    controllers.add(clazz);
                }
            }
        }
    }

    public static void findAllClasses(String packageName, List<Class<?>> classesList) {
        packageName = packageName.replaceAll("[.]", "/");
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        try (InputStream in = classLoader.getResourceAsStream(packageName);
             InputStreamReader isr = new InputStreamReader(in);
             BufferedReader reader = new BufferedReader(isr)) {

            addClasses(packageName, classesList, reader);
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading package: " + packageName);
        }
    }

    private static void addClasses(String packageName, List<Class<?>> classesList, BufferedReader reader) throws IOException {
        for (String line; (line = reader.readLine()) != null;) {
            if (!line.endsWith(".class")) {
                findAllClasses(packageName + "." + line, classesList);
                continue;
            }

            line = line.substring(0, line.lastIndexOf('.'));
            packageName = packageName.replace("/", ".");
            String className = packageName + "." + line;

            try {
                Class<?> clazz = Class.forName(className);
                classesList.add(clazz);
            } catch (ClassNotFoundException e) {
                throw  new IllegalStateException("Class not found: " + className);
            }
        }
    }

    private static void loadProperties(String packageName) throws IOException {
        packageName = packageName.replace(".", "/");
        String fileName = String.format(APP_PROPERTIES_FILE_NAME, packageName);
        try (FileReader fileReader = new FileReader(fileName)) {
            appProperties.load(fileReader);
        }
    }
}
