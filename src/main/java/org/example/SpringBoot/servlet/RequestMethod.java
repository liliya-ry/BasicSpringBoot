package org.example.SpringBoot.servlet;

import org.example.SpringContainer.annotations.web.*;

import java.lang.reflect.*;

public class RequestMethod {
    Method method;
    Object controller;
    ParamInfo[] paramInfos;
    boolean toBeSerialized;

    RequestMethod(Method method, Object controller, boolean isRestController) {
        this.method = method;
        this.controller = controller;
        processParams();
        toBeSerialized = isRestController || method.getAnnotation(ResponseBody.class) != null;
    }

    Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(controller, args);
    }

    private void processParams() {
        Parameter[] params = method.getParameters();
        paramInfos = new ParamInfo[params.length];
        for (int i = 0; i < params.length; i++) {
            paramInfos[i] = new ParamInfo(params[i].getType());

            PathVariable pathVariableAnn = params[i].getAnnotation(PathVariable.class);
            if (pathVariableAnn != null) {
                paramInfos[i].isPathVariable = true;
                paramInfos[i].requestParamName = params[i].getName();
                continue;
            }

            RequestBody requestBodyAnn = params[i].getAnnotation(RequestBody.class);
            if (requestBodyAnn != null) {
                paramInfos[i].isFromRequestBody = true;
                continue;
            }

            RequestParam requestParamAnn = params[i].getAnnotation(RequestParam.class);
            if (requestParamAnn != null) {
                paramInfos[i].requestParamName = params[i].getName();
            }
        }
    }
}
