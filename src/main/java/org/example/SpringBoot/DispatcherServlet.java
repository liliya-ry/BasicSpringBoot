package org.example.SpringBoot;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.example.SpringContainer.annotations.beans.Autowired;
import org.example.SpringContainer.annotations.web.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.regex.*;

import static jakarta.servlet.http.HttpServletResponse.*;

public class DispatcherServlet extends HttpServlet {
    @Autowired
    MappingsContainer mappingsContainer;
    @Autowired
    Gson gson;

    // TODO: test original and make error classes - serialize them in json and return
    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String key = req.getMethod() + req.getPathInfo();
        RequestMethod requestMethod = mappingsContainer.simpleRequestMappings.get(key);
        if (requestMethod == null)
            requestMethod = getRequestMethod(key);

        if (requestMethod == null) {
            resp.setStatus(SC_BAD_REQUEST);
            return;
        }

        try {
            Object[] args = getArgs(req, requestMethod);
            Object result = requestMethod.invoke(args);
            String json = gson.toJson(result);
            resp.getWriter().write(json);
        } catch (InvocationTargetException | IllegalAccessException e) {
            resp.sendError(SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Object[] getArgs(HttpServletRequest req, RequestMethod requestMethod) throws IOException {
        Parameter[] params = requestMethod.method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            PathVariable pathVariableAnn = params[i].getAnnotation(PathVariable.class);
            if (pathVariableAnn != null) {
                //arg=
                continue;
            }

            RequestBody requestBodyAnn = params[i].getAnnotation(RequestBody.class);
            if (requestBodyAnn != null) {
                args[i] = gson.fromJson(req.getReader(), params[i].getType());
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
}
