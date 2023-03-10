package SpringBoot.application;

import org.apache.ibatis.session.*;

import java.lang.reflect.*;

public class ProxySession implements InvocationHandler {
    SqlSessionFactory sqlSessionFactory;
    Class<?> mapperClass;

    ProxySession(Class<?> mapperClass, SqlSessionFactory sqlSessionFactory) {
        this.mapperClass = mapperClass;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            return method.invoke(sqlSession, args);
        }
    }
}
