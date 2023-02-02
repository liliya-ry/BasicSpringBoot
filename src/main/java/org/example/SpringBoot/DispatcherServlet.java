package org.example.SpringBoot;

import static jakarta.servlet.http.HttpServletResponse.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    MappingsContainer mappingsContainer;

    public DispatcherServlet() throws Exception {
        mappingsContainer = new MappingsContainer();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, RequestMethod> mappingsMap =
                switch (req.getMethod()) {
                    case "GET" -> mappingsContainer.getMappings;
                    case "POST" -> mappingsContainer.postMappings;
                    case "PUT" -> mappingsContainer.putMappings;
                    case "DELETE" -> mappingsContainer.deleteMappings;
                    default -> mappingsContainer.serviceMappings;
                };
        processRequest(req, resp, mappingsMap);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse resp, Map<String, RequestMethod> mappingsMap) throws IOException {
        RequestMethod requestMethod = mappingsMap.get(request.getPathInfo());
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
