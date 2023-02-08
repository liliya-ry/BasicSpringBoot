package org.example.SpringFramework.SpringBoot.servlet;

import org.example.SpringFramework.SpringBoot.interceptors.HandlerMethod;
import org.example.SpringFramework.SpringContainer.annotations.web.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class RequestMethod implements HandlerMethod {
    Method method;
    Object controller;
    List<ParamInfo> paramInfos;
    String methodType;
    boolean toBeSerialized;

    RequestMethod(Method method, Object controller, boolean isRestController, String methodType) {
        this.method = method;
        this.controller = controller;
        processParams();
        toBeSerialized = isRestController || method.getAnnotation(ResponseBody.class) != null;
        this.methodType = methodType;
    }

    Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(controller, args);
    }

    private void processParams() {
        Parameter[] params = method.getParameters();
        paramInfos = new ArrayList<>(params.length);

        for (Parameter param : params) {
            ParamInfo paramInfo = new ParamInfo(param.getType());

            if (param.isAnnotationPresent(PathVariable.class)) {
                paramInfo.isPathVariable = true;
                paramInfo.requestParamName = param.getName();
            }

            if (param.isAnnotationPresent(RequestBody.class)) {
                paramInfo.isFromRequestBody = true;
            }

            if (param.isAnnotationPresent(RequestParam.class)) {
                paramInfo.requestParamName = param.getName();
            }

            paramInfos.add(paramInfo);
        }
    }

    @Override
    public Annotation getMethodAnnotation(Class annotationType) {
        return method.getAnnotation(annotationType);
    }
}
