package org.example.SpringFramework.SpringBoot.servlet;

import org.example.SpringFramework.SpringBoot.application.ControllersDispatcher;
import org.example.SpringFramework.SpringBoot.application.SpringApplication;
import org.example.SpringFramework.SpringContainer.annotations.web.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class MappingsContainer {
    List<PathInfo> pathInfos = new ArrayList<>();
    List<org.example.SpringFramework.SpringBoot.servlet.RequestMethod> requestMethods = new ArrayList<>();

    public MappingsContainer() throws Exception {
        ControllersDispatcher controllersDispatcher = SpringApplication.getAppContext().getBean(ControllersDispatcher.class);
        processControllerClasses(controllersDispatcher.getRestControllers(), true);
        processControllerClasses(controllersDispatcher.getControllers(), false);
    }

    private void processControllerClasses(List<Class<?>> classesList, boolean isRestController) throws Exception {
        for (Class<?> clazz : classesList) {
            Class<?> controllerClass = getControllerClass(clazz);
            RequestMapping requestMappingAnn = controllerClass.getAnnotation(RequestMapping.class);

            String controllerPath = requestMappingAnn == null ? "" : requestMappingAnn.value();

            Object controller = SpringApplication.getAppContext().getBean(clazz);
            for (Method method : controllerClass.getDeclaredMethods())
                allocateMappings(method, controllerPath, controller, isRestController);
        }
    }

    private Class<?> getControllerClass(Class<?> clazz) {
        for (Class<?> interfaceClass : clazz.getInterfaces()) {
            if (interfaceClass.isAnnotationPresent(RequestMapping.class))
                return interfaceClass;
        }
        return clazz;
    }

    private void allocateMappings(Method method, String controllerPath, Object instance, boolean isRestController) {
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            String annType = annotation.annotationType().getSimpleName();
            String requestPath = getRequestPath(controllerPath, annotation, annType);

            if (requestPath == null)
                continue;

            PathInfo pathInfo = new PathInfo(requestPath);
            pathInfos.add(pathInfo);
            RequestMethod requestMethod = new RequestMethod(method, instance, isRestController, pathInfo.getMethodType());
            requestMethods.add(requestMethod);
        }
    }

    private String getRequestPath(String controllerPath, Annotation annotation, String annType) {
        return switch (annType) {
            case "GetMapping" -> "GET" + controllerPath + ((GetMapping) annotation).value();
            case "PostMapping" -> "POST" + controllerPath +((PostMapping) annotation).value();
            case "PutMapping" -> "PUT" + controllerPath + ((PutMapping) annotation).value();
            case "DeleteMapping" -> "DELETE" + controllerPath + ((DeleteMapping) annotation).value();
            case "RequestMapping" -> ((RequestMapping) annotation).method() + controllerPath + ((RequestMapping) annotation).value();
            default -> null;
        };
    }
}
