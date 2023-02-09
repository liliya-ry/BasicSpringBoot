package SpringBoot.application;

import SpringBoot.annotations.*;
import SpringContainer.SpringContainer;
import SpringContainer.annotations.beans.*;
import org.apache.ibatis.io.Resources;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ApplicationContext {
    private static final String APP_PROPERTIES_FILE_NAME = "application.properties";
    Properties appProperties;
    final SpringContainer springContainer = new SpringContainer();
    final Set<Class<?>> classesSet = new HashSet<>();

    ApplicationContext(Class<?> configurationClass) throws Exception {
        Set<String> packages = findPackages(configurationClass);
        for (String packageName : packages) {
            findAllClasses(packageName, classesSet);
        }

        appProperties = Resources.getResourceAsProperties(APP_PROPERTIES_FILE_NAME);
    }

    private Set<String> findPackages(Class<?> configurationClass) {
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

    private void findAllClasses(String packageName, Set<Class<?>> classesSet) {
        packageName = packageName.replaceAll("[.]", "/");

        if (!packageName.equals(""))
            packageName = packageName.substring(1);

        if (packageName.equals("application/properties"))
            return;

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(packageName);
             InputStreamReader isr = new InputStreamReader(in);
             BufferedReader reader = new BufferedReader(isr)) {

            addClasses(packageName, classesSet, reader);
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading package: " + packageName);
        }
    }

    private void addClasses(String packageName, Set<Class<?>> classesSet, BufferedReader reader) throws IOException {
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

    public SpringContainer getSpringContainer() {
        return springContainer;
    }

    public Properties getAppProperties() {
        return appProperties;
    }

    public Set<Class<?>> getClassesSet() {
        return classesSet;
    }
}
