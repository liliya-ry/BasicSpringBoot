package org.example.SpringBoot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RequestMethod {
    Method method;
    Object instance;
    Object[] args;

    RequestMethod(Method method, Object instance, Object[] args) {
        this.method = method;
        this.instance = instance;
        this.args = args;
    }

    Object invoke() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(instance, args);
    }
}
