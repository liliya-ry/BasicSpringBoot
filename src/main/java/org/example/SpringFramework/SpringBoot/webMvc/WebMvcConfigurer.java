package org.example.SpringFramework.SpringBoot.webMvc;

import org.example.SpringFramework.SpringBoot.interceptors.InterceptorRegistry;

public interface WebMvcConfigurer {
    void addInterceptors(InterceptorRegistry registry) throws Exception;
}
