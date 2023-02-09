import SpringBoot.interceptors.InterceptorRegistry;
import SpringBoot.webMvc.WebMvcConfigurer;
import SpringContainer.annotations.beans.*;
import auth.AuthInterceptor;
import logging.LoggingInterceptor;

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
