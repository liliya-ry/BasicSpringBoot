package org.example.SpringBoot;

import org.example.SpringContainer.annotations.web.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MappingsContainer {
    Map<String, RequestMethod> getMappings = new HashMap<>();
    Map<String, RequestMethod> postMappings = new HashMap<>();
    Map<String, RequestMethod> putMappings = new HashMap<>();
    Map<String, RequestMethod> deleteMappings = new HashMap<>();
    Map<String, RequestMethod> serviceMappings = new HashMap<>();

    public MappingsContainer() throws Exception {
        for (Class<?> controllerClass : SpringApplication.controllers) {
            RequestMapping requestMappingAnn = controllerClass.getAnnotation(RequestMapping.class);
            String controllerPath = requestMappingAnn.value();
            Object instance = controllerClass.getDeclaredConstructor(controllerClass).newInstance();
            for (Method method : controllerClass.getDeclaredMethods()) {
                allocateMappings(method, controllerPath, instance);
            }
        }
    }

    private void allocateMappings(Method method, String controllerPath, Object instance) {
        RequestMethod requestMethod = new RequestMethod(method, instance, method.getParameters());
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            String annType = annotation.annotationType().getSimpleName();
            switch (annType) {
                case "GetMapping" -> getMappings.put(controllerPath + ((GetMapping) annotation).value(), requestMethod);
                case "PostMapping" -> postMappings.put(controllerPath + ((PostMapping) annotation).value(), requestMethod);
                case "PutMapping" -> putMappings.put(controllerPath + ((PutMapping) annotation).value(), requestMethod);
                case "DeleteMapping" -> deleteMappings.put(controllerPath + ((DeleteMapping) annotation).value(), requestMethod);
                default -> serviceMappings.put(controllerPath + ((DeleteMapping) annotation).value(), requestMethod);
            }
        }
    }
}
