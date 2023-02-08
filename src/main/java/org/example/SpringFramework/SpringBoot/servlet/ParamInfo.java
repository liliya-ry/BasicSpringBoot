package org.example.SpringFramework.SpringBoot.servlet;

public class ParamInfo {
    Class<?> type;
    boolean isPathVariable = false;
    boolean isFromRequestBody = false;
    String requestParamName = null;

    ParamInfo(Class<?> type) {
        this.type = type;
    }
}
