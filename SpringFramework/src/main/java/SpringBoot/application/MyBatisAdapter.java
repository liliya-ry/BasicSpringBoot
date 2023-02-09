package SpringBoot.application;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.datasource.pooled.*;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.*;

public class MyBatisAdapter implements Adapter {
    private Configuration myBatisConfig;
    private SqlSessionFactory sqlSessionFactory;
    private PooledDataSource dataSource;


    public void configure(ApplicationContext appContext) throws Exception {
        dataSource = createDataSource(appContext.appProperties);
        myBatisConfig = createMyBatisConfig();
        sqlSessionFactory = createSessionFactory(myBatisConfig);
        registerMappers(appContext);
    }

    private Configuration createMyBatisConfig() {
        Environment environment = new Environment("Development", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        return configuration;
    }

    private PooledDataSource createDataSource(Properties appProperties) {
        String driver = appProperties.getProperty("spring.datasource.driverClassName");
        String url = appProperties.getProperty("spring.datasource.url");
        String username = appProperties.getProperty("spring.datasource.username");
        String password = appProperties.getProperty("spring.datasource.password");
        return new PooledDataSource(driver, url, username, password);
    }

    private SqlSessionFactory createSessionFactory(Configuration myBatisConfig) {
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        return builder.build(myBatisConfig);
    }

    void registerMappers(ApplicationContext appContext) throws Exception {
        for (Class<?> clazz : appContext.classesSet) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (!(a instanceof Mapper))
                    continue;

                myBatisConfig.addMapper(clazz);
                var handler = new ProxySession(clazz, sqlSessionFactory);
                SqlSession sqlSession =  (SqlSession) Proxy.newProxyInstance(
                        ClassLoader.getSystemClassLoader(),
                        new Class[]{SqlSession.class}, handler);
                Object mapper = myBatisConfig.getMapper(clazz, sqlSession);
                appContext.springContainer.getBean(clazz, mapper);
            }
        }
    }
}
