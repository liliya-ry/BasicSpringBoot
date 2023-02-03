package org.example.SpringBoot;

import java.lang.reflect.*;

public class RequestMethod {
    Method method;
    Object controller;
    Parameter[] params;

    RequestMethod(Method method, Object controller, Parameter[] params) {
        this.method = method;
        this.controller = controller;
        this.params = params;
    }

    Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(controller, args);
    }
}
