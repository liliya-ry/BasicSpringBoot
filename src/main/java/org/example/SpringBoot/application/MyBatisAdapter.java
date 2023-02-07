package org.example.SpringBoot.application;

import org.apache.ibatis.datasource.pooled.*;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.*;

import javax.sql.DataSource;
import java.util.Properties;

public class MyBatisAdapter {
    Configuration configuration;
    SqlSessionFactory sqlSessionFactory;

    MyBatisAdapter(Properties appProperties) {
        DataSource dataSource = createDataSource(appProperties);
        Environment environment = new Environment("Development", new JdbcTransactionFactory(), dataSource);
        configuration = new Configuration(environment);
        configuration.setMapUnderscoreToCamelCase(true);
        createSessionFactory();
    }

    private DataSource createDataSource(Properties appProperties) {
        String driver = appProperties.getProperty("spring.datasource.driverClassName");
        String url = appProperties.getProperty("spring.datasource.url");
        String username = appProperties.getProperty("spring.datasource.username");
        String password = appProperties.getProperty("spring.datasource.password");
        return new PooledDataSource(driver, url, username, password);
    }

    private void createSessionFactory() {
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        sqlSessionFactory = builder.build(configuration);
    }
}
