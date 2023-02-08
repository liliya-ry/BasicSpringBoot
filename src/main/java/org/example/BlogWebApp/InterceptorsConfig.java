package org.example.BlogWebApp;

import org.example.BlogWebApp.auth.AuthInterceptor;
import org.example.BlogWebApp.logging.LoggingInterceptor;
import org.example.SpringFramework.SpringBoot.interceptors.InterceptorRegistry;
import org.example.SpringFramework.SpringBoot.webMvc.WebMvcConfigurer;
import org.example.SpringFramework.SpringContainer.annotations.beans.Autowired;
import org.example.SpringFramework.SpringContainer.annotations.beans.Configuration;

@Configuration
public class InterceptorsConfig implements WebMvcConfigurer {
    @Autowired
    private LoggingInterceptor loggingInterceptor;
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) throws Exception {
        registry.addInterceptor(authInterceptor).excludePathPatterns("/users/register");
        registry.addInterceptor(loggingInterceptor);
    }
}
