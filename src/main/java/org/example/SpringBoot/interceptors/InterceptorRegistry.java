package org.example.SpringBoot.interceptors;

import org.example.BlogWebApp.auth.AuthInterceptor;

import java.util.ArrayList;
import java.util.List;

public class InterceptorRegistry {
    private List<HandlerInterceptor> interceptors = new ArrayList<>();

    public InterceptorRegistry addInterceptor(HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    public void excludePathPatterns(String s) {
    }
}
