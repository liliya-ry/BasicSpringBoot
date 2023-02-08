package org.example.SpringFramework.SpringBoot.application;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.datasource.pooled.*;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.*;
import org.example.SpringFramework.SpringContainer.ApplicationContext;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.*;

public class MyBatisAdapter {
    private final Configuration myBatisConfig;
    private final SqlSessionFactory sqlSessionFactory;

    MyBatisAdapter(Properties appProperties) throws Exception {
        myBatisConfig = createMyBatisConfig(appProperties);
        sqlSessionFactory = createSessionFactory(myBatisConfig);
    }

    private Configuration createMyBatisConfig(Properties appProperties) {
        DataSource dataSource = createDataSource(appProperties);
        Environment environment = new Environment("Development", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        return configuration;
    }

    private DataSource createDataSource(Properties appProperties) {
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

    void registerMappers(ApplicationContext appContext, Set<Class<?>> classesSet) throws Exception {
        for (Class<?> clazz : classesSet) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (!(a instanceof Mapper))
                    continue;

                myBatisConfig.addMapper(clazz);
                var handler = new ProxySession(clazz, sqlSessionFactory);
                SqlSession sqlSession =  (SqlSession) Proxy.newProxyInstance(
                        ClassLoader.getSystemClassLoader(),
                        new Class[]{SqlSession.class}, handler);
                Object mapper = myBatisConfig.getMapper(clazz, sqlSession);
                appContext.getBean(clazz, mapper);
            }
        }
    }
}
