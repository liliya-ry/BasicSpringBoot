package org.example.SpringBoot.servlet;

import static jakarta.servlet.http.HttpServletResponse.*;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.example.SpringContainer.annotations.beans.Autowired;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.regex.*;

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
        if (requestMethod == null)
            requestMethod = getRequestMethod(key);

        if (requestMethod == null) {
            sendServerError(req, resp);
            return;
        }

        try {
            Object[] args = getArgs(req, requestMethod);
            Object result = requestMethod.invoke(args);
            String responseToWrite = requestMethod.toBeSerialized ? gson.toJson(result) : result.toString();
            resp.getWriter().write(responseToWrite);
        } catch (InvocationTargetException | IllegalAccessException e) {
            sendServerError(req, resp);
        }
    }

    private boolean isMethodSupported(String method) {
        return method.equals("GET") || method.equals("POST") || method.equals("PUT") || method.equals("DELETE");
    }

    private Object[] getArgs(HttpServletRequest req, RequestMethod requestMethod) throws IOException {
        ParamInfo[] paramInfos = requestMethod.paramInfos;
        Object[] args = new Object[paramInfos.length];

        for (int i = 0; i < paramInfos.length; i++) {
            if (paramInfos[i].isPathVariable) {
                //arg= //TODO fix regex ans extract value
                continue;
            }

            if (paramInfos[i].isFromRequestBody) {
                args[i] = requestMethod.toBeSerialized ?
                          gson.fromJson(req.getReader(), paramInfos[i].type) :
                          new String(req.getInputStream().readAllBytes());
                continue;
            }

            if (paramInfos[i].requestParamName != null) {
                args[i] = getRequestParam(req, paramInfos[i]);
            }
        }

        return args;
    }

    private RequestMethod getRequestMethod(String key) {
        for (int i = 0; i < mappingsContainer.requestPatterns.size(); i++) {
            Pattern requestPattern = mappingsContainer.requestPatterns.get(i);
            Matcher matcher = requestPattern.matcher(key);
            if (matcher.matches()) {
                return mappingsContainer.requestMethods.get(i);
            }
        }
        return null;
    }

    private Object getRequestParam(HttpServletRequest req, ParamInfo paramInfo) {
        String requestParam = req.getParameter(paramInfo.requestParamName);
        return switch (paramInfo.type.getSimpleName()) {
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
