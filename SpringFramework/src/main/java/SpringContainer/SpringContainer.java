package SpringContainer;

import SpringContainer.annotations.beans.*;
import SpringContainer.events.EventListener;
import SpringContainer.events.*;
import SpringContainer.exceptions.*;
import org.mockito.Mockito;
import java.lang.reflect.*;
import java.util.*;

public class SpringContainer {
    private final Map<String, Object> namedInstances = new HashMap<>();
    private final Map<Class<?>, Object> classInstances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();
    private Set<Class<?>> visitedClasses = new HashSet<>();
    private ApplicationEventPublisher appEventPublisher;

    public SpringContainer() {}

    public SpringContainer(Properties properties) {
        properties.forEach((k, v) -> namedInstances.put((String) k, v));
    }

    public Object getBean(String key) {
        return namedInstances.get(key);
    }

    public <T> T getBean(Class<T> c) throws Exception {
        T instance = (T) classInstances.get(c);

        if (instance == null) {
            visitedClasses.add(c);
            instance = (T) createBean(c);
            getBean(c, instance);
        }

        return instance;
    }

    public void getBean(String key, Object instance) throws Exception {
        Object existingInstance = namedInstances.get(key);

        if (existingInstance != null) {
            throw new ConfigurationException("Instance with name " + key + " already exists");
        }

        namedInstances.put(key, instance);
    }

    public void getBean(Class<?> c, Object instance) throws Exception {
        Object existingInstance = classInstances.get(c);

        if (existingInstance != null) {
            throw new ConfigurationException("Instance for " + c.getSimpleName() + " already exists");
        }

        classInstances.put(c, instance);
    }

    public void getBean(Object instance) throws Exception {
        classInstances.put(instance.getClass(), instance);
    }

    public void registerImplementation(Class<?> c, Class<?> subClass) throws Exception {
        if (!c.isAssignableFrom(subClass)) {
            throw new ConfigurationException(c + " is not assignable from " + subClass);
        }
        implementations.put(c, subClass);
    }

    public void registerImplementation(Class<?> c) throws Exception {
        Class<?>[] interfaces = c.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            registerImplementation(interfaceClass, c);
        }
    }

    private Object createBean(Class<?> c) throws Exception {
        c = getClassForConstructor(c);
        if (c == null)
            throw new ConfigurationException("Missing default interface implementation for interface");

        Object instance = injectConstructor(c);
        if (instance == null) {
            Constructor<?> constructor = c.getDeclaredConstructor();
            instance = constructor.newInstance();
            decorateInstance(instance);
        }

        visitedClasses = new HashSet<>();

        return instance;
    }

    public void decorateInstance(Object o) throws Exception {
        extractEvents(o);
        injectFields(o);

        if (o instanceof Initializer)
            ((Initializer) o).init();
    }

    private void injectFields(Object o) throws Exception {
        Class<?> instanceClass = o.getClass();
        Field[] fields = instanceClass.getDeclaredFields();
        for (Field field : fields) {
            Autowired autowiredAnn = field.getAnnotation(Autowired.class);
            if (autowiredAnn == null) {
                continue;
            }

            if (o instanceof ApplicationEventPublisher) {
                continue;
            }

            if (field.getType().equals(ApplicationEventPublisher.class)) {
                appEventPublisher = new ApplicationEventPublisher(this);
                setFieldValue(field, o, appEventPublisher);
                continue;
            }

            Qualifier qualifierAnn = field.getAnnotation(Qualifier.class);
            Lazy lazyAnn = field.getAnnotation(Lazy.class);

            if (lazyAnn != null) {
                Object value = createMockObject(o, field, qualifierAnn);
                setFieldValue(field, o, value);
                continue;
            }

            if (qualifierAnn != null) {
                Object value = getBean(field.getName());
                setFieldValue(field, o, value);
                continue;
            }

            Class<?> fieldType = field.getType();
            Object value = visitedClasses.contains(fieldType) ?
                    createMockObject(o, field, qualifierAnn) :
                    getBean(fieldType);
            setFieldValue(field, o, value);
        }
    }

    private Object createMockObject(Object o, Field field, Qualifier qualifierAnn) {
        return Mockito.mock(field.getType(), invocation -> {
            Object value = qualifierAnn != null ?
                    getBean(field.getName()) :
                    getBean(field.getType());
            setFieldValue(field, o, value);
            return invocation.getMethod().invoke(value, invocation.getArguments());
        });
    }

    private void setFieldValue(Field field, Object o, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(o, value);
    }

    private Class<?> getClassForConstructor(Class<?> c) throws Exception {
        if (!c.isInterface()) {
            return c;
        }

        Class<?> implClass = implementations.get(c);
        if (implClass != null) {
            return implClass;
        }

        Default defaultAnn = c.getAnnotation(Default.class);
        if (defaultAnn == null) {
            return null;
        }

        implClass = defaultAnn.value();
        if (!c.isAssignableFrom(implClass)) {
            throw new ConfigurationException(c + "is not assignable from " + implClass);
        }

        registerImplementation(c, implClass);
        return implClass;
    }

    private Object injectConstructor(Class<?> instanceClass) throws Exception {
        boolean hasAutowiredAnn = false;
        Object instance = null;

        Constructor<?>[] constructors = instanceClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            Autowired autowiredAnn = constructor.getAnnotation(Autowired.class);
            if (autowiredAnn == null) {
                continue;
            }

            if (hasAutowiredAnn) {
                throw new ConfigurationException("More than one constructor with @Inject annotation");
            }

            hasAutowiredAnn = true;
            Object[] values = getParamValues(constructor);
            instance = constructor.newInstance(values);
        }

        return instance;
    }

    private static final Set<Class<?>> WRAPPER_CLASSES = Set.of(
            Integer.class, Float.class, Double.class, Short.class,
            Long.class, Byte.class, Character.class, Boolean.class
    );

    private Object[] getParamValues(Constructor<?> constructor) throws Exception {
        Parameter[] parameters = constructor.getParameters();
        Object[] values = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterType = parameters[i].getType();
            if (isNotWrapperPrimitiveOrString(parameterType)) {
                values[i] = getBean(parameterType);
                continue;
            }

            Qualifier qualifierAnn = parameters[i].getAnnotation(Qualifier.class);
            if (qualifierAnn == null) {
                throw new ConfigurationException("Unnamed primitive parameter of type " + parameterType);
            }

            String propertyName = qualifierAnn.value();
            String property = (String) namedInstances.get(propertyName);

            if (property == null) {
                throw new ConfigurationException("Missing property " + propertyName);
            }

            values[i] = fromStringToPrimitive(property, parameterType);
        }

        return values;
    }

    private boolean isNotWrapperPrimitiveOrString(Class<?> type) {
        return !type.isPrimitive() && !WRAPPER_CLASSES.contains(type) && !type.equals(String.class);
    }

    private Object fromStringToPrimitive(String s, Class<?> type) {
        return switch (type.getSimpleName()) {
            case "int", "Integer" -> Integer.parseInt(s);
            case "double", "Double" -> Double.parseDouble(s);
            case "float", "Float" -> Float.parseFloat(s);
            case "boolean", "Boolean" -> Boolean.parseBoolean(s);
            case "short", "Short" -> Short.parseShort(s);
            case "long", "Long" -> Long.parseLong(s);
            case "byte", "Byte" -> Byte.parseByte(s);
            case "char", "Char" -> s.charAt(0);
            default -> s;
        };
    }

    private void extractEvents(Object instance) {
        if (instance instanceof ApplicationEventPublisher) {
            return;
        }

        for (Method method : instance.getClass().getDeclaredMethods()) {
            EventListener eventListenerAnn = method.getAnnotation(EventListener.class);
            if (eventListenerAnn == null) {
                continue;
            }

            Parameter eventParam = method.getParameters()[0];
            Listener eventListener = new Listener(instance, method, eventParam.getType());
            appEventPublisher.addListener(eventListener);
        }
    }
}