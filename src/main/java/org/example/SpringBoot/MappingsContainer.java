package org.example.SpringBoot;

import org.example.SpringContainer.annotations.web.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static org.example.SpringContainer.annotations.web.RequestMethod.*;

public class MappingsContainer {
    private static final String VARIABLE_PATTERN_STR = "\\{\\w+}";
    private static final String VARIABLE_PATTERN_STR_ESCAPED = "\\\\{\\\\w+}"; //TODO: fix pattern

    Map<String, RequestMethod> simpleRequestMappings = new HashMap<>();
    List<Pattern> requestPatterns = new ArrayList<>();
    List<RequestMethod> requestMethods = new ArrayList<>();

    public MappingsContainer() throws Exception {
        for (Class<?> clazz : SpringApplication.controllers) {
            Class<?> controllerClass = getControllerClass(clazz);
            RequestMapping requestMappingAnn = controllerClass.getAnnotation(RequestMapping.class);

            if (requestMappingAnn == null)
                continue;

            String controllerPath = requestMappingAnn.value();

            Object controller = SpringApplication.SPRING_CONTAINER.getInstance(clazz);
            for (Method method : controllerClass.getDeclaredMethods()) {
                allocateMappings(method, controllerPath, controller);
            }
        }
    }

    private Class<?> getControllerClass(Class<?> clazz) {
        for (Class<?> interfaceClass : clazz.getInterfaces()) {
            if (interfaceClass.getAnnotation(RequestMapping.class) != null)
                return interfaceClass;
        }
        return clazz;
    }

    private void allocateMappings(Method method, String controllerPath, Object instance) {
        RequestMethod requestMethod = new RequestMethod(method, instance);
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

            if (requestPath == null)
                continue;


            String regexStr = requestPath.replaceAll(VARIABLE_PATTERN_STR, VARIABLE_PATTERN_STR_ESCAPED);

            if (requestPath.equals(regexStr)) {
                simpleRequestMappings.put(requestPath, requestMethod);
                continue;
            }

            Pattern requestPattern = Pattern.compile(regexStr);
            requestPatterns.add(requestPattern);
            requestMethods.add(requestMethod);
        }
    }
}
