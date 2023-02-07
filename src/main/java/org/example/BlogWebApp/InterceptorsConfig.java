package org.example.BlogWebApp;

import org.example.BlogWebApp.auth.AuthInterceptor;
import org.example.BlogWebApp.logging.LoggingInterceptor;
import org.example.SpringBoot.application.SpringApplication;
import org.example.SpringBoot.interceptors.InterceptorRegistry;
import org.example.SpringBoot.webMvc.WebMvcConfigurer;
import org.example.SpringContainer.annotations.beans.*;

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
