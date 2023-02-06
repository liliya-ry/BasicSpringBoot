package org.example.SpringBoot.application;

import static org.example.SpringBoot.application.SpringApplication.*;

import com.google.gson.*;
import org.apache.ibatis.annotations.*;
import org.example.SpringContainer.*;
import org.example.SpringContainer.annotations.beans.*;

import java.lang.annotation.*;
import java.util.*;

public class SpringAdapter {
    private static final Set<Class<?>> BEAN_TYPES = Set.of(Component.class, RestController.class);

    private final Container springContainer = new Container();
    private List<Class<?>> controllers = new ArrayList<>();
    private List<Class<?>> restControllers = new ArrayList<>();

    SpringAdapter(List<Class<?>> classesList) throws Exception {
        registerMappers(classesList);
        allocateBeans(classesList);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        springContainer.registerInstance(Gson.class, gson);
    }

    public List<Class<?>> getControllers() {
        return controllers;
    }

    public List<Class<?>> getRestControllers() {
        return restControllers;
    }

    public Container getSpringContainer() {
        return springContainer;
    }

    private void registerMappers(List<Class<?>> classesList) throws Exception {
        for (Class<?> clazz : classesList) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (!(a instanceof Mapper))
                    continue;

                myBatisAdapter.configuration.addMapper(clazz);
                Object mapper = myBatisAdapter.configuration.getMapper(clazz, myBatisAdapter.sqlSession);
                springContainer.registerInstance(clazz, mapper);
            }
        }
    }

    private void allocateBeans(List<Class<?>> classesList) throws Exception {
        for (Class<?> clazz : classesList) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (a instanceof Mapper)
                    continue;

                if (BEAN_TYPES.contains(a.getClass())) {
                    springContainer.getInstance(clazz);
                }

                if (a instanceof RestController) {
                    restControllers.add(clazz);
                    continue;
                }

                if (a instanceof Controller) {
                    controllers.add(clazz);
                }
            }
        }
    }
}
