package org.example.SpringFramework.SpringBoot.interceptors;

import jakarta.servlet.http.*;
import java.util.*;

public abstract class HandlerInterceptor {
    private Set<String> excludedPaths = new HashSet<>();

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)  {}

    public void excludePathPatterns(String path) {
        excludedPaths.add(path);
    }

    public Set<String> getExcludedPaths() {
        return excludedPaths;
    }
}
