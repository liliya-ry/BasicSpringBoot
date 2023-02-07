package org.example.SpringBoot.interceptors;

import java.lang.annotation.Annotation;

public interface HandlerMethod {
    Annotation getMethodAnnotation(Class annotationType);
}
