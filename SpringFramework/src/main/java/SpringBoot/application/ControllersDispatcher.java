package SpringBoot.application;

import SpringBoot.interceptors.InterceptorRegistry;
import SpringBoot.webMvc.WebMvcConfigurer;
import SpringContainer.annotations.beans.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class ControllersDispatcher {
    private final List<Class<?>> controllers = new ArrayList<>();
    private final List<Class<?>> restControllers = new ArrayList<>();

    ControllersDispatcher(ApplicationContext appContext) throws Exception {
        InterceptorRegistry registry = new InterceptorRegistry();

        for (Class<?> clazz : appContext.classesSet) {
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
                method.invoke(appContext.springContainer.getBean(clazz), registry);
            }
        }

        appContext.springContainer.getBean(InterceptorRegistry.class, registry);
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
