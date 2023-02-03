package org.example.SpringBoot;

import org.example.SpringContainer.annotations.web.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static org.example.SpringContainer.annotations.web.RequestMethod.*;

public class MappingsContainer {
    Map<String, RequestMethod> requestMappings = new HashMap<>();

    public MappingsContainer() throws Exception {
        for (Class<?> controllerClass : SpringApplication.controllers) {
            RequestMapping requestMappingAnn = controllerClass.getAnnotation(RequestMapping.class);
            String controllerPath = requestMappingAnn.value();
            Object controller = SpringApplication.SPRING_CONTAINER.getInstance(controllerClass);
            for (Method method : controllerClass.getDeclaredMethods()) {
                allocateMappings(method, controllerPath, controller);
            }
        }
    }

    private void allocateMappings(Method method, String controllerPath, Object instance) {
        RequestMethod requestMethod = new RequestMethod(method, instance, method.getParameters());
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            String annType = annotation.annotationType().getSimpleName();

            String requestPath = switch (annType) {
                case "GetMapping" -> GET + controllerPath + ((GetMapping) annotation).value();
                case "PostMapping" -> POST + controllerPath +((PostMapping) annotation).value();
                case "PutMapping" -> PUT + controllerPath + ((PutMapping) annotation).value();
                case "DeleteMapping" -> DELETE + controllerPath + ((DeleteMapping) annotation).value();
                case "RequestMapping" -> ((RequestMapping) annotation).method() + controllerPath + ((RequestMapping) annotation).value();
                default -> null;
            };

            if (requestPath != null)
                requestMappings.put(requestPath, requestMethod);
        }
    }
}
