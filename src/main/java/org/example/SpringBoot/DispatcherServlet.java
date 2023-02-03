package org.example.SpringBoot;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.example.SpringContainer.annotations.beans.Autowired;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class DispatcherServlet extends HttpServlet {
    @Autowired
    MappingsContainer mappingsContainer;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println(req.getMethod());
        System.out.println(req.getPathInfo());
        String key = req.getMethod() + req.getPathInfo();
        RequestMethod requestMethod = mappingsContainer.requestMappings.get(key);
        if (requestMethod == null) {
            resp.sendError(SC_BAD_REQUEST);
            return;
        }

        Object result;
        try {
            result = requestMethod.invoke();
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
