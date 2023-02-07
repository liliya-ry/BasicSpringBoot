package org.example.SpringBoot.application;

import org.example.SpringBoot.annotations.*;
import org.example.SpringContainer.annotations.beans.*;

import java.io.*;
import java.util.*;

public class SpringApplication {
    private static final String APP_PROPERTIES_FILE_NAME = "src/main/java/%s/resources/application.properties";
    private static final Properties appProperties = new Properties();

    static Set<Class<?>> classesList = new HashSet<>();
    static SpringAdapter springAdapter;
    static MyBatisAdapter myBatisAdapter;

    public static void run(Class<?> configurationClass, String[] arguments) throws Exception {
        Set<String> packages = findPackages(configurationClass);
        for (String packageName : packages)
            findAllClasses(packageName, classesList);
        loadProperties(configurationClass.getPackageName());

        myBatisAdapter = new MyBatisAdapter(appProperties);
        springAdapter = new SpringAdapter(classesList);

        TomcatAdapter tomcatAdapter = new TomcatAdapter(appProperties);
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
            appProperties.load(fileReader);
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
            } catch (ClassNotFoundException e) {
                throw  new IllegalStateException("Class not found: " + className);
            }
        }
    }

    public static SpringAdapter getSpringAdapter() {
        return springAdapter;
    }
}
