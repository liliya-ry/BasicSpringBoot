package org.example.SpringBoot;

import org.example.SpringContainer.annotations.web.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class MappingsContainer {
    Map<String, Method> getMappings = new HashMap<>();
    Map<String, Method> postMappings = new HashMap<>();
    Map<String, Method> putMappings = new HashMap<>();
    Map<String, Method> deleteMappings = new HashMap<>();

    public MappingsContainer() {
        for (Class<?> controllerClass : SpringApplication.controllers) {
            RequestMapping requestMappingAnn = controllerClass.getAnnotation(RequestMapping.class);
            String controllerPath = requestMappingAnn.value();
            for (Method method : controllerClass.getDeclaredMethods()) {
                allocateMappings(method, method.getDeclaredAnnotations(), controllerPath);
            }
        }
    }

    private void allocateMappings(Method method, Annotation[] annotations, String controllerPath) {
        for (Annotation annotation : annotations) {
            String annType = annotation.annotationType().getSimpleName();
            switch (annType) {
                case "GetMapping" -> getMappings.put(controllerPath + ((GetMapping) annotation).value(), method);
                case "PostMapping" -> postMappings.put(controllerPath + ((PostMapping) annotation).value(), method);
                case "PutMapping" -> putMappings.put(controllerPath + ((PutMapping) annotation).value(), method);
                case "DeleteMapping" -> deleteMappings.put(controllerPath + ((DeleteMapping) annotation).value(), method);
            }
        }
    }
}
