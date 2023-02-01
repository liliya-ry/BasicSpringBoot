package org.example.SpringBoot.interceptors;

public interface HandlerMethod {
    Object getMethodAnnotation(Class<?> annotationType);
}
