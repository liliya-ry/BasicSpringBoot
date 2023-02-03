package org.example.SpringBoot;

import com.google.gson.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.example.SpringContainer.Container;
import org.example.SpringContainer.annotations.beans.*;

import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.*;

import javax.sql.DataSource;
import java.io.*;
import java.lang.annotation.*;
import java.util.*;

public class SpringApplication {
    private static final String DEFAULT_TOMCAT_PORT = "8080";
    private static final String DEFAULT_CONTEXT_PATH = "";
    private static final String APP_PROPERTIES_FILE_NAME = "src/main/java/%s/resources/application.properties";
    private static final Properties appProperties = new Properties();
    private static final Set<Class<?>> BEAN_TYPES = Set.of(Component.class, RestController.class);
    static final Container SPRING_CONTAINER = new Container();
    static List<Class<?>> controllers = new ArrayList<>();
    static Configuration mybatisConfiguration;
    private static SqlSession sqlSession;
    private static Tomcat tomcat;

    public static void run(Class<?> configurationClass, String[] arguments) throws Exception {
        loadProperties(configurationClass.getPackageName());
        setUpMyBatis();
        initSession();
        setUpSpringContainer(configurationClass);

        setUpTomcat();
        tomcat.start();
    }

    private static void setUpTomcat() throws Exception {
        tomcat = new Tomcat();
        String portStr = appProperties.getProperty("server.port", DEFAULT_TOMCAT_PORT);
        int port = Integer.parseInt(portStr);
        tomcat.setPort(port);
        tomcat.getConnector();

        String contextPath = appProperties.getProperty("server.servlet.context-path", DEFAULT_CONTEXT_PATH);
        Context context = tomcat.addContext(contextPath, null);

        addDispatcherServlet(tomcat, contextPath, context);
    }

    private static void addDispatcherServlet(Tomcat tomcat, String contextPath, Context context) throws Exception {
        String servletName = DispatcherServlet.class.getSimpleName();
        HttpServlet servlet = SPRING_CONTAINER.getInstance(DispatcherServlet.class);
        tomcat.addServlet(contextPath, servletName, servlet);
        context.addServletMappingDecoded("/*", servletName);
    }

    private static void setUpSpringContainer(Class<?> configurationClass) throws Exception {
        List<Class<?>> classesList = new ArrayList<>();
        findAllClasses(configurationClass.getPackageName(), classesList);
        registerMappers(classesList);
        allocateBeans(classesList);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SPRING_CONTAINER.registerInstance(Gson.class, gson);
    }

    private static void registerMappers(List<Class<?>> classesList) throws Exception {
        for (Class<?> clazz : classesList) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (!(a instanceof Mapper))
                    continue;

                mybatisConfiguration.addMapper(clazz);
                Object mapper = mybatisConfiguration.getMapper(clazz, sqlSession);
                SPRING_CONTAINER.registerInstance(clazz, mapper);
            }
        }
    }

    private static void allocateBeans(List<Class<?>> classesList) throws Exception {
        for (Class<?> clazz : classesList) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (a instanceof Mapper)
                    continue;

                if (BEAN_TYPES.contains(a.getClass())) {
                    SPRING_CONTAINER.getInstance(clazz);
                }

                if (a instanceof RestController) {
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

    private static void setUpMyBatis() {
        DataSource dataSource = createDataSource();
        Environment environment = new Environment("Development", new JdbcTransactionFactory(), dataSource);
        mybatisConfiguration = new Configuration(environment);
        mybatisConfiguration.setMapUnderscoreToCamelCase(true);
    }

    private static DataSource createDataSource() {
        String driver = appProperties.getProperty("spring.datasource.driverClassName");
        String url = appProperties.getProperty("spring.datasource.url");
        String username = appProperties.getProperty("spring.datasource.username");
        String password = appProperties.getProperty("spring.datasource.password");
        return new PooledDataSource(driver, url, username, password);
    }

    private static void initSession() {
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = builder.build(mybatisConfiguration);
        sqlSession = sqlSessionFactory.openSession();
    }
}
