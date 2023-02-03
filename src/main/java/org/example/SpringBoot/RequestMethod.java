package org.example.SpringBoot;

import java.lang.reflect.*;

public class RequestMethod {
    Method method;
    Object controller;

    RequestMethod(Method method, Object controller) {
        this.method = method;
        this.controller = controller;
    }

    Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(controller, args);
    }
}
