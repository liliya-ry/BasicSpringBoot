package SpringBoot.application;

import SpringContainer.annotations.beans.*;
import com.google.gson.*;

import java.lang.annotation.*;
import java.util.*;

public class SpringInjector {
    private static final Set<Class<?>> BEAN_TYPES = Set.of(Component.class, RestController.class, Controller.class, Configuration.class);

    private final ApplicationContext appContext;

    SpringInjector(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    void injectSpring() throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        appContext.springContainer.getBean(Gson.class, gson);
        allocateBeans();
    }

    private void allocateBeans() throws Exception {
        for (Class<?> clazz : appContext.classesSet) {
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (BEAN_TYPES.contains(a.getClass())) {
                    appContext.springContainer.getBean(clazz);
                }
            }
        }
    }
}
