package org.example.SpringFramework.SpringBoot.application;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxySession implements InvocationHandler {
    SqlSessionFactory sqlSessionFactory;
    Class<?> mapperClass;

    ProxySession(Class<?> mapperClass, SqlSessionFactory sqlSessionFactory) {
        this.mapperClass = mapperClass;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        return method.invoke(sqlSession, args);
    }
}
