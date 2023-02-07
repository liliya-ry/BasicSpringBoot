package org.example.SpringBoot.interceptors;

import org.example.BlogWebApp.auth.AuthInterceptor;
import org.example.SpringContainer.annotations.beans.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InterceptorRegistry {
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();

    public InterceptorRegistry addInterceptor(HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    public void excludePathPatterns(String s) {
    }

    public List<HandlerInterceptor> getInterceptors() {
        return interceptors;
    }
}
