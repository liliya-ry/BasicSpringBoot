package org.example.SpringFramework.SpringBoot.servlet;

import static jakarta.servlet.http.HttpServletResponse.*;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.example.SpringFramework.SpringBoot.servlet.errors.ServerError;
import org.example.SpringFramework.SpringContainer.annotations.beans.Autowired;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.List;

public class DispatcherServlet extends HttpServlet {
    @Autowired
    Gson gson;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestMethod requestMethod = (RequestMethod) req.getAttribute("requestMethod");

        try {
            Object[] args = getArgs(req, requestMethod);
            Object result = requestMethod.invoke(args);
            String responseToWrite = result.toString();
            if (requestMethod.toBeSerialized) {
                responseToWrite = gson.toJson(result);
                resp.setContentType("application/json");
            }
            resp.getWriter().write(responseToWrite);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            sendServerError(req, resp);
            //TODO: @ControllerAdvice exceptions handling
        }
    }

    private Object[] getArgs(HttpServletRequest req, RequestMethod requestMethod) throws IOException {
        List<ParamInfo> paramInfos = requestMethod.paramInfos;
        Object[] args = new Object[paramInfos.size()];

        for (int i = 0; i < paramInfos.size(); i++) {
            ParamInfo paramInfo = paramInfos.get(i);
            if (paramInfo.isPathVariable) {
                String pathVar = (String) req.getAttribute(paramInfo.requestParamName);
                args[i] = getRequestParam(pathVar, paramInfo.type);
                continue;
            }

            if (paramInfo.isFromRequestBody) {
                args[i] = requestMethod.toBeSerialized ?
                          gson.fromJson(req.getReader(), paramInfo.type) :
                          new String(req.getInputStream().readAllBytes());
                continue;
            }

            if (paramInfo.requestParamName != null) {
                String requestParam = req.getParameter(paramInfo.requestParamName);
                args[i] = getRequestParam(requestParam, paramInfo.type);
            }
        }

        return args;
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

    private void sendError(Object error, HttpServletResponse resp, int status) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        String responseToWrite = gson.toJson(error);
        resp.getWriter().write(responseToWrite);
    }
}
