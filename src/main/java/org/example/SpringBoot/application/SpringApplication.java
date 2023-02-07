package org.example.SpringBoot.application;

import org.example.SpringBoot.annotations.*;
import org.example.SpringContainer.ApplicationContext;
import org.example.SpringContainer.annotations.beans.*;

import java.io.*;
import java.util.*;

public class SpringApplication {
    private static final String APP_PROPERTIES_FILE_NAME = "src/main/java/%s/resources/application.properties";
    private static final Properties APP_PROPERTIES = new Properties();
    private static final ApplicationContext APP_CONTEXT = new ApplicationContext();
    private static final Set<Class<?>> CLASSES_SET = new HashSet<>();

    public static void run(Class<?> configurationClass, String[] arguments) throws Exception {
        Set<String> packages = findPackages(configurationClass);
        for (String packageName : packages)
            findAllClasses(packageName, CLASSES_SET);

        loadProperties(configurationClass.getPackageName());

        MyBatisAdapter myBatisAdapter = new MyBatisAdapter(APP_PROPERTIES);
        myBatisAdapter.registerMappers(APP_CONTEXT, CLASSES_SET);

        SpringInjector springInjector = new SpringInjector();
        springInjector.registerGson(APP_CONTEXT);
        springInjector.allocateBeans(APP_CONTEXT, CLASSES_SET);

        ControllersDispatcher controllerDispatcher = new ControllersDispatcher(CLASSES_SET, APP_CONTEXT);
        APP_CONTEXT.getBean(ControllersDispatcher.class, controllerDispatcher);

        TomcatAdapter tomcatAdapter = new TomcatAdapter(APP_PROPERTIES, APP_CONTEXT);
        tomcatAdapter.startServer();
    }

    private static Set<String> findPackages(Class<?> configurationClass) {
        if (configurationClass.getAnnotation(SpringBootApplication.class) != null)
            return Set.of(configurationClass.getPackageName());

        Set<String> packages = new HashSet<>();
        Configuration confAnn = configurationClass.getAnnotation(Configuration.class);
        if (confAnn == null)
            return packages;

        if (configurationClass.getAnnotation(EnableAutoConfiguration.class) != null) {
            packages.add(configurationClass.getPackageName());
        }

        ComponentScan componentScanAnn = configurationClass.getAnnotation(ComponentScan.class);
        if (componentScanAnn == null)
            return packages;

        String[] basePackages = componentScanAnn.basePackages();
        if (basePackages.length == 1 && basePackages[0].isEmpty()) {
            packages.add(configurationClass.getPackageName());
        } else {
            Collections.addAll(packages, basePackages);
        }

        return packages;
    }

    private static void loadProperties(String packageName) throws IOException {
        packageName = packageName.replace(".", "/");
        String fileName = String.format(APP_PROPERTIES_FILE_NAME, packageName);
        try (FileReader fileReader = new FileReader(fileName)) {
            APP_PROPERTIES.load(fileReader);
        }
    }

    public static void findAllClasses(String packageName, Set<Class<?>> classesSet) {
        packageName = packageName.replaceAll("[.]", "/");
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        try (InputStream in = classLoader.getResourceAsStream(packageName);
             InputStreamReader isr = new InputStreamReader(in);
             BufferedReader reader = new BufferedReader(isr)) {

            addClasses(packageName, classesSet, reader);
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading package: " + packageName);
        }
    }

    private static void addClasses(String packageName, Set<Class<?>> classesSet, BufferedReader reader) throws IOException {
        for (String line; (line = reader.readLine()) != null;) {
            if (!line.endsWith(".class")) {
                findAllClasses(packageName + "." + line, classesSet);
                continue;
            }

            line = line.substring(0, line.lastIndexOf('.'));
            packageName = packageName.replace("/", ".");
            String className = packageName + "." + line;

            try {
                Class<?> clazz = Class.forName(className);
                classesSet.add(clazz);
            } catch (ClassNotFoundException ignored) {}
        }
    }

    public static ApplicationContext getAppContext() {
        return APP_CONTEXT;
    }
}
