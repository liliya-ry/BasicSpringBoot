package SpringBoot.webMvc;

import SpringBoot.interceptors.InterceptorRegistry;

public interface WebMvcConfigurer {
    void addInterceptors(InterceptorRegistry registry) throws Exception;
}
