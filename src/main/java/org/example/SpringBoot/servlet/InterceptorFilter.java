package org.example.SpringBoot.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.SpringBoot.interceptors.HandlerInterceptor;
import org.example.SpringBoot.interceptors.InterceptorRegistry;
import org.example.SpringContainer.annotations.beans.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InterceptorFilter implements Filter {
    @Autowired
    InterceptorRegistry interceptorRegistry;
    @Autowired
    MappingsContainer mappingsContainer;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        String key = request.getMethod() + request.getPathInfo();

        RequestMethod requestMethod = mappingsContainer.simpleRequestMappings.get(key);

        Map<String, String> pathVariables = new HashMap<>();
        if (requestMethod == null) {
            requestMethod = getRequestMethod(key, pathVariables);
        }

        boolean doInvoke = true;
        for (HandlerInterceptor interceptor : interceptorRegistry.getInterceptors()) {
            doInvoke = interceptor.preHandle(request, response, requestMethod);
            if (!doInvoke)
                break;
        }

        if (!doInvoke)
            return;

        Exception ex = null;
        try {
            request.setAttribute("requestMethod", requestMethod);
            for (Map.Entry<String, String> pathVarEntry : pathVariables.entrySet()) {
                request.setAttribute(pathVarEntry.getKey(), pathVarEntry.getValue());
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            ex = e;
        }

        for (HandlerInterceptor interceptor : interceptorRegistry.getInterceptors()) {
            interceptor.afterCompletion((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, requestMethod, ex);
        }
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
}
