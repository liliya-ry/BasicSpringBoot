package org.example.SpringBoot.servlet;

import static jakarta.servlet.http.HttpServletResponse.*;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.example.SpringContainer.annotations.beans.Autowired;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    @Autowired
    MappingsContainer mappingsContainer;
    @Autowired
    Gson gson;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (!isMethodSupported(method)) {
            sendNotSupportedError(req, resp);
            return;
        }

        String key = method + req.getPathInfo();

        RequestMethod requestMethod = mappingsContainer.simpleRequestMappings.get(key);

        Map<String, String> pathVariables = new HashMap<>();
        if (requestMethod == null) {
            requestMethod = getRequestMethod(key, pathVariables);
        }

        if (requestMethod == null) {
            sendServerError(req, resp);
            return;
        }

        try {
            Object[] args = getArgs(req, requestMethod, pathVariables);
            Object result = requestMethod.invoke(args);
            String responseToWrite = requestMethod.toBeSerialized ? gson.toJson(result) : result.toString();
            resp.getWriter().write(responseToWrite);
        } catch (InvocationTargetException | IllegalAccessException e) {
            sendServerError(req, resp);
            //TODO: @ControllerAdvice exceptions handling
        }
    }

    private boolean isMethodSupported(String method) {
        return method.equals("GET") || method.equals("POST") || method.equals("PUT") || method.equals("DELETE");
    }

    private Object[] getArgs(HttpServletRequest req, RequestMethod requestMethod, Map<String, String> pathVariables) throws IOException {
        ParamInfo[] paramInfos = requestMethod.paramInfos;
        Object[] args = new Object[paramInfos.length];

        for (int i = 0; i < paramInfos.length; i++) {
            if (paramInfos[i].isPathVariable) {
                String pathVar = pathVariables.get(paramInfos[i].requestParamName);
                args[i] = getRequestParam(pathVar, paramInfos[i].type);
                continue;
            }

            if (paramInfos[i].isFromRequestBody) {
                args[i] = requestMethod.toBeSerialized ?
                          gson.fromJson(req.getReader(), paramInfos[i].type) :
                          new String(req.getInputStream().readAllBytes());
                continue;
            }

            if (paramInfos[i].requestParamName != null) {
                String requestParam = req.getParameter(paramInfos[i].requestParamName);
                args[i] = getRequestParam(requestParam, paramInfos[i].type);
            }
        }

        return args;
    }

    private RequestMethod getRequestMethod(String key, Map<String, String> pathVariables) {
        for (int i = 0; i < mappingsContainer.pathInfos.size(); i++) {
            PathInfo pathInfo = mappingsContainer.pathInfos.get(i);
            String[] pathParts = key.split("/");

            if (pathParts.length != pathInfo.pathParts.length)
                continue;

            boolean matchesPathInfo = true;
            for (int j = 0; j < pathParts.length; j++) {
                if (pathParts[j].equals(pathInfo.pathParts[j]))
                    continue;

                if (pathInfo.paramNames[j] == null) {
                    matchesPathInfo = false;
                    break;
                }

                pathVariables.put(pathInfo.paramNames[j], pathParts[j]);
            }

            if (matchesPathInfo)
                return  mappingsContainer.requestMethods.get(i);
        }

        return null;
    }

    private Object getRequestParam(String requestParam, Class<?> paramType) {
        return switch (paramType.getSimpleName()) {
            case "Integer", "int" -> Integer.parseInt(requestParam);
            case "Double", "double" -> Double.parseDouble(requestParam);
            case "Float", "float" -> Float.parseFloat(requestParam);
            case "Long", "long" -> Long.parseLong(requestParam);
            case "Short", "short" -> Short.parseShort(requestParam);
            case "Byte", "byte" -> Byte.parseByte(requestParam);
            case "Boolean", "boolean" -> Boolean.parseBoolean(requestParam);
            case "Character", "char" -> requestParam.charAt(0);
            default -> requestParam;
        };
    }

    private void sendServerError(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object serverError = new ServerError(req.getPathInfo());
        sendError(serverError, resp, SC_INTERNAL_SERVER_ERROR);
    }

    private void sendNotSupportedError(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object notSupportedError = new NotSupportedError(req.getMethod(), req.getPathInfo());
        sendError(notSupportedError, resp, SC_METHOD_NOT_ALLOWED);
    }

    private void sendError(Object error, HttpServletResponse resp, int status) throws IOException {
        resp.setStatus(status);
        String responseToWrite = gson.toJson(error);
        resp.getWriter().write(responseToWrite);
    }
}
