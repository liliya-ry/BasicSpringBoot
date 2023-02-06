package org.example.SpringBoot.application;

import java.io.*;
import java.util.*;

public class SpringApplication {
    private static final String APP_PROPERTIES_FILE_NAME = "src/main/java/%s/resources/application.properties";
    private static final Properties appProperties = new Properties();

    static List<Class<?>> classesList = new ArrayList<>();
    static SpringAdapter springAdapter;
    static MyBatisAdapter myBatisAdapter;

    public static void run(Class<?> configurationClass, String[] arguments) throws Exception {
        findAllClasses(configurationClass.getPackageName(), classesList);
        loadProperties(configurationClass.getPackageName());

        myBatisAdapter = new MyBatisAdapter(appProperties);
        springAdapter = new SpringAdapter(classesList);

        TomcatAdapter tomcatAdapter = new TomcatAdapter(appProperties);
        tomcatAdapter.startServer();
    }

    private static void loadProperties(String packageName) throws IOException {
        packageName = packageName.replace(".", "/");
        String fileName = String.format(APP_PROPERTIES_FILE_NAME, packageName);
        try (FileReader fileReader = new FileReader(fileName)) {
            appProperties.load(fileReader);
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

    public static SpringAdapter getSpringAdapter() {
        return springAdapter;
    }
}
