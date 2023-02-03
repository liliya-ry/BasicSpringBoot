package org.example.SpringBoot;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.example.SpringContainer.annotations.beans.Autowired;
import org.example.SpringContainer.annotations.web.PathVariable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jakarta.servlet.http.HttpServletResponse.*;

public class DispatcherServlet extends HttpServlet {
    @Autowired
    MappingsContainer mappingsContainer;
    @Autowired
    Gson gson;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String key = req.getMethod() + req.getPathInfo();
        RequestMethod requestMethod = mappingsContainer.simpleRequestMappings.get(key);
        if (requestMethod == null)
            requestMethod = getRequestMethod(key);

        if (requestMethod == null)
            resp.setStatus(SC_BAD_REQUEST);

        try {
            for (Parameter parameter : requestMethod.method.getParameters()) {
                PathVariable pathVariableAnn = parameter.getAnnotation(PathVariable.class);
                if (pathVariableAnn != null) {

                }
            }
            Object result = requestMethod.invoke();
            String json = gson.toJson(result);
            resp.getWriter().write(json);
        } catch (InvocationTargetException | IllegalAccessException e) {
            resp.sendError(SC_INTERNAL_SERVER_ERROR);
        }
    }

    private RequestMethod getRequestMethod(String key) {
        for (int i = 0; i < mappingsContainer.requestPatterns.size(); i++) {
            Pattern requestPattern = mappingsContainer.requestPatterns.get(i);
            Matcher matcher = requestPattern.matcher(key);
            if (matcher.matches())
                return mappingsContainer.requestMethods.get(i);

        }
        return null;
    }
}
