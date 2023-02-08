package org.example.SpringFramework.SpringBoot.application;

import org.example.SpringFramework.SpringBoot.interceptors.InterceptorRegistry;
import org.example.SpringFramework.SpringBoot.webMvc.WebMvcConfigurer;
import org.example.SpringFramework.SpringContainer.ApplicationContext;
import org.example.SpringContainer.annotations.beans.*;
import org.example.SpringFramework.SpringContainer.annotations.beans.Controller;
import org.example.SpringFramework.SpringContainer.annotations.beans.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class ControllersDispatcher {
    private final List<Class<?>> controllers = new ArrayList<>();
    private final List<Class<?>> restControllers = new ArrayList<>();

    ControllersDispatcher(Set<Class<?>> classesSet, ApplicationContext appContext) throws Exception {
        InterceptorRegistry registry = new InterceptorRegistry();

        for (Class<?> clazz : classesSet) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (a instanceof RestController) {
                    restControllers.add(clazz);
                    continue;
                }

                if (a instanceof Controller) {
                    controllers.add(clazz);
                    continue;
                }

                if (!isMvcConfig(clazz))
                    continue;

                Method method = clazz.getMethod("addInterceptors", InterceptorRegistry.class);
                method.invoke(appContext.getBean(clazz), registry);
            }
        }

        appContext.getBean(InterceptorRegistry.class, registry);
    }

    private boolean isMvcConfig(Class<?> clazz) {
        for (Class<?> interfaceClass : clazz.getInterfaces())
            if (interfaceClass.equals(WebMvcConfigurer.class))
                return true;

        return false;
    }

    public List<Class<?>> getControllers() {
        return controllers;
    }

    public List<Class<?>> getRestControllers() {
        return restControllers;
    }
}
