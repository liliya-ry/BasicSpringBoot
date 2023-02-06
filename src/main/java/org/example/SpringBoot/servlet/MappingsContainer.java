package org.example.SpringBoot.servlet;

import static org.example.SpringContainer.annotations.web.RequestMethod.*;

import org.example.SpringBoot.application.SpringApplication;
import org.example.SpringContainer.annotations.web.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingsContainer {
    private static final String VARIABLE_REGEX = "\\{([\\w\\d]+)}";
    static final Pattern VARIABLE_PATTERN = Pattern.compile(VARIABLE_REGEX);

    Map<String, RequestMethod> simpleRequestMappings = new HashMap<>();
    List<PathInfo> pathInfos = new ArrayList<>();
    List<RequestMethod> requestMethods = new ArrayList<>();

    public MappingsContainer() throws Exception {
        processControllerClasses(SpringApplication.getSpringAdapter().getRestControllers(), true);
        processControllerClasses(SpringApplication.getSpringAdapter().getControllers(), false);
    }

    private void processControllerClasses(List<Class<?>> classesList, boolean isRestController) throws Exception {
        for (Class<?> clazz : classesList) {
            Class<?> controllerClass = getControllerClass(clazz);
            RequestMapping requestMappingAnn = controllerClass.getAnnotation(RequestMapping.class);

            if (requestMappingAnn == null)
                continue;

            String controllerPath = requestMappingAnn.value();

            Object controller = SpringApplication.getSpringAdapter().getSpringContainer().getBean(clazz);
            for (Method method : controllerClass.getDeclaredMethods()) {
                allocateMappings(method, controllerPath, controller, isRestController);
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

    private void allocateMappings(Method method, String controllerPath, Object instance, boolean isRestController) {
        RequestMethod requestMethod = new RequestMethod(method, instance, isRestController);
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            String annType = annotation.annotationType().getSimpleName();
            String requestPath = getRequestPath(controllerPath, annotation, annType);

            if (requestPath == null)
                continue;

            Matcher matcher = VARIABLE_PATTERN.matcher(requestPath);

            if (!matcher.find()) {
                simpleRequestMappings.put(requestPath, requestMethod);
                continue;
            }

            PathInfo pathInfo = new PathInfo(requestPath);
            pathInfos.add(pathInfo);
            requestMethods.add(requestMethod);
        }
    }

    private String getRequestPath(String controllerPath, Annotation annotation, String annType) {
        return switch (annType) {
            case "GetMapping" -> GET + controllerPath + ((GetMapping) annotation).value();
            case "PostMapping" -> POST + controllerPath +((PostMapping) annotation).value();
            case "PutMapping" -> PUT + controllerPath + ((PutMapping) annotation).value();
            case "DeleteMapping" -> DELETE + controllerPath + ((DeleteMapping) annotation).value();
            case "RequestMapping" -> ((RequestMapping) annotation).method() + controllerPath + ((RequestMapping) annotation).value();
            default -> null;
        };
    }
}
