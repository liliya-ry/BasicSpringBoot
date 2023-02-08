package org.example.SpringFramework.SpringBoot.servlet;

import static jakarta.servlet.http.HttpServletResponse.*;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.example.SpringBoot.interceptors.*;
import org.example.SpringFramework.SpringBoot.interceptors.HandlerInterceptor;
import org.example.SpringFramework.SpringBoot.interceptors.InterceptorRegistry;
import org.example.SpringFramework.SpringBoot.servlet.errors.NotSupportedError;
import org.example.SpringFramework.SpringBoot.servlet.errors.ServerError;
import org.example.SpringFramework.SpringContainer.annotations.beans.Autowired;

import java.io.*;
import java.util.*;

public class InterceptorFilter implements Filter {
    @Autowired
    Gson gson;
    @Autowired
    InterceptorRegistry interceptorRegistry;
    @Autowired
    MappingsContainer mappingsContainer;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        String requestPath = request.getMethod() + request.getPathInfo();
        RequestMethod requestMethod = getRequestMethod(requestPath, request);

        if (requestMethod == null) {
            sendMissingRequestMethodError(request, response);
            return;
        }

        request.setAttribute("requestMethod", requestMethod);
        boolean doInvoke = invokePreHandleMethods(request, response, requestMethod);

        if (!doInvoke) {
            sendServerError(request, response);
            return;
        }

        Exception ex = null;
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            ex = e;
        }

        invokeAfterCompletionMethods(request, response, requestMethod, ex);
    }

    private void sendMissingRequestMethodError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean isNotSupported = (boolean) request.getAttribute("methodNotSupported");
        if (isNotSupported) {
            sendNotSupportedError(request, response);
        } else {
            sendServerError(request, response);
        }
    }

    private void invokeAfterCompletionMethods(HttpServletRequest request, HttpServletResponse response, RequestMethod requestMethod, Exception ex) {
        for (HandlerInterceptor interceptor : interceptorRegistry.getInterceptors()) {
            boolean isPathExcluded = interceptor.getExcludedPaths().contains(request.getPathInfo());
            if (isPathExcluded)
                continue;

            interceptor.afterCompletion(request, response, requestMethod, ex);
        }
    }

    private boolean invokePreHandleMethods(HttpServletRequest request, HttpServletResponse response, RequestMethod requestMethod) {
        boolean doInvoke = true;
        for (HandlerInterceptor interceptor : interceptorRegistry.getInterceptors()) {
            if (!doInvoke)
                continue;

            boolean isPathExcluded = interceptor.getExcludedPaths().contains(request.getPathInfo());
            if (isPathExcluded)
                continue;

            doInvoke = interceptor.preHandle(request, response, requestMethod);
        }
        return doInvoke;
    }

    private RequestMethod getRequestMethod(String requestPath, HttpServletRequest request) {
        List<RequestMethod> requestMethodList = new ArrayList<>();
        getAllMatchingMethods(requestPath, request, requestMethodList);

        for (RequestMethod requestMethod : requestMethodList)
            if (request.getMethod().equals(requestMethod.methodType))
                return requestMethod;

        request.setAttribute("methodNotSupported", true);
        return null;
    }

    private void getAllMatchingMethods(String requestPath, HttpServletRequest request, List<RequestMethod> requestMethodList) {
        for (int i = 0; i < mappingsContainer.pathInfos.size(); i++) {
            PathInfo pathInfo = mappingsContainer.pathInfos.get(i);
            String[] pathParts = requestPath.split("/");

            if (pathParts.length != pathInfo.pathParts.length)
                continue;

            boolean matchesPathInfo = true;
            for (int j = 1; j < pathParts.length; j++) {
                if (pathParts[j].equals(pathInfo.pathParts[j]))
                    continue;

                if (pathInfo.paramNames[j] == null) {
                    matchesPathInfo = false;
                    break;
                }

                request.setAttribute(pathInfo.paramNames[j], pathParts[j]);
            }

            if (matchesPathInfo) {
                RequestMethod requestMethod = mappingsContainer.requestMethods.get(i);
                requestMethodList.add(requestMethod);
            }
        }
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
        resp.setContentType("application/json");
        String responseToWrite = gson.toJson(error);
        resp.getWriter().write(responseToWrite);
    }
}