package org.example.SpringBoot;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.example.SpringContainer.Container;
import org.example.SpringContainer.annotations.beans.*;

import java.io.*;
import java.lang.annotation.*;
import java.util.*;

public class SpringApplication {
    static final Container CONTAINER = new Container();
    static List<Class<?>> controllers = new ArrayList<>();
    private static final Set<Class<?>> BEAN_TYPES = Set.of(Component.class);

    public static void run(Class<?> configurationClass, String[] arguments) throws Exception {
        initContext(configurationClass);
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8082);
        Context context = tomcat.addContext("/blogApp", null);
        tomcat.addServlet("/blogApp", DispatcherServlet.class.getSimpleName(), DispatcherServlet.class.toString());
        context.addServletMappingDecoded("/blogApp/*", DispatcherServlet.class.getSimpleName());
        tomcat.start();
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
}
