package org.example.SpringBoot.webMvc;

import org.example.SpringBoot.interceptors.InterceptorRegistry;

public interface WebMvcConfigurer {
    void addInterceptors(InterceptorRegistry registry);
}
