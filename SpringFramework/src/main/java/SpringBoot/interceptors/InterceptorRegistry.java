package SpringBoot.interceptors;

import SpringContainer.annotations.beans.Component;

import java.util.*;

@Component
public class InterceptorRegistry {
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();

    public HandlerInterceptor addInterceptor(HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
        return interceptor;
    }

    public List<HandlerInterceptor> getInterceptors() {
        return interceptors;
    }
}
