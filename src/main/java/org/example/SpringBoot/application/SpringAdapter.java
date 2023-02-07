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

    private final ApplicationContext applicationContext = new ApplicationContext();
    private final List<Class<?>> controllers = new ArrayList<>();
    private final List<Class<?>> restControllers = new ArrayList<>();

    SpringAdapter(Set<Class<?>> classesSet) throws Exception {
        registerMappers(classesSet);
        allocateBeans(classesSet);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        applicationContext.getBean(Gson.class, gson);
    }

    public List<Class<?>> getControllers() {
        return controllers;
    }

    public List<Class<?>> getRestControllers() {
        return restControllers;
    }

    public ApplicationContext getSpringContainer() {
        return applicationContext;
    }

    private void registerMappers(Set<Class<?>> classesSet) throws Exception {
        for (Class<?> clazz : classesSet) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (!(a instanceof Mapper))
                    continue;

                myBatisAdapter.configuration.addMapper(clazz);
                Object mapper = myBatisAdapter.configuration.getMapper(clazz, myBatisAdapter.sqlSessionFactory.openSession());
                applicationContext.getBean(clazz, mapper);
            }
        }
    }

    private void allocateBeans(Set<Class<?>> classesSet) throws Exception {
        for (Class<?> clazz : classesSet) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (a instanceof Mapper)
                    continue;

                if (BEAN_TYPES.contains(a.getClass())) {
                    applicationContext.getBean(clazz);
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
