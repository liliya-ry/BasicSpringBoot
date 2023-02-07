package org.example.SpringBoot.application;

import com.google.gson.*;
import org.example.SpringContainer.*;
import org.example.SpringContainer.annotations.beans.*;

import java.lang.annotation.*;
import java.util.*;

public class SpringInjector {
    private static final Set<Class<?>> BEAN_TYPES = Set.of(Component.class, RestController.class);

    void registerGson(ApplicationContext appContext) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        appContext.getBean(Gson.class, gson);
    }

    void allocateBeans(ApplicationContext appContext, Set<Class<?>> classesSet) throws Exception {
        for (Class<?> clazz : classesSet) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (BEAN_TYPES.contains(a.getClass())) {
                    appContext.getBean(clazz);
                }
            }
        }
    }
}
