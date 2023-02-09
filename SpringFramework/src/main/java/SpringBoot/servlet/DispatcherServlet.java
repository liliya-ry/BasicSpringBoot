package SpringBoot.servlet;

import static jakarta.servlet.http.HttpServletResponse.*;

import SpringBoot.servlet.errors.ServerError;
import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import SpringContainer.annotations.beans.Autowired;

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
            sendError(resp, SC_NOT_FOUND, e.getCause().getMessage());
        } catch (IllegalArgumentException e) {
            Object serverError = new ServerError(req.getPathInfo());
            sendError(resp, SC_BAD_REQUEST, serverError);
        }
    }

    private Object[] getArgs(HttpServletRequest req, RequestMethod requestMethod) throws IOException {
        List<ParamInfo> paramInfos = requestMethod.paramInfos;
        Object[] args = new Object[paramInfos.size()];

        for (int i = 0; i < paramInfos.size(); i++) {
            ParamInfo paramInfo = paramInfos.get(i);
            if (paramInfo.isPathVariable) {
                args[i] = getPathVarValue(req, paramInfo);
                continue;
            }

            if (paramInfo.isFromRequestBody) {
                args[i] = getRequestBodyValue(req, paramInfo, requestMethod);
                continue;
            }

            if (paramInfo.requestParamName != null) {
                args[i] = getRequestParamValue(req, paramInfo);
            }
        }

        return args;
    }

    private Object getRequestParamValue(HttpServletRequest req, ParamInfo paramInfo) {
        String requestParam = req.getParameter(paramInfo.requestParamName);
        if (requestParam == null)
            throw new IllegalArgumentException();

        return getRequestParam(requestParam, paramInfo.type);
    }

    private Object getRequestBodyValue(HttpServletRequest req, ParamInfo paramInfo, RequestMethod requestMethod) throws IOException {
        if (!req.getContentType().equals("application/json"))
            throw new IllegalArgumentException();

        return requestMethod.toBeSerialized ?
                  gson.fromJson(req.getReader(), paramInfo.type) :
                  new String(req.getInputStream().readAllBytes());
    }

    private Object getPathVarValue(HttpServletRequest req, ParamInfo paramInfo) {
        String pathVar = (String) req.getAttribute(paramInfo.requestParamName);
        if (pathVar == null)
            throw new IllegalArgumentException();

        return getRequestParam(pathVar, paramInfo.type);
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

    private void sendError(HttpServletResponse resp, int status, Object error) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        String responseToWrite = error instanceof String errorStr ? errorStr : gson.toJson(error);
        resp.getWriter().write(responseToWrite);
    }
}
