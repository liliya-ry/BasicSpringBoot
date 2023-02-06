package org.example.SpringBoot.application;

import org.apache.ibatis.datasource.pooled.*;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.*;

import javax.sql.DataSource;
import java.util.Properties;

public class MyBatisAdapter {
    Configuration configuration;
    SqlSession sqlSession;

    MyBatisAdapter(Properties appProperties) {
        DataSource dataSource = createDataSource(appProperties);
        Environment environment = new Environment("Development", new JdbcTransactionFactory(), dataSource);
        configuration = new Configuration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        createSession();
    }

    private DataSource createDataSource(Properties appProperties) {
        String driver = appProperties.getProperty("spring.datasource.driverClassName");
        String url = appProperties.getProperty("spring.datasource.url");
        String username = appProperties.getProperty("spring.datasource.username");
        String password = appProperties.getProperty("spring.datasource.password");
        return new PooledDataSource(driver, url, username, password);
    }

    private void createSession() {
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = builder.build(configuration);
        sqlSession = sqlSessionFactory.openSession();
    }
}
