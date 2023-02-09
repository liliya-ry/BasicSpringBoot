package SpringContainer.events;

import SpringContainer.exceptions.ConfigurationException;
import SpringContainer.SpringContainer;
import SpringContainer.annotations.beans.Async;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Executor;

public class ApplicationEventPublisher {
    private final List<Listener> listeners = new ArrayList<>();
    private final Executor pool;


    public ApplicationEventPublisher(SpringContainer container) throws ConfigurationException {
        ApplicationEventMulticaster multicaster = (ApplicationEventMulticaster) container.getBean("applicationEventMulticaster");
        if (multicaster == null) {
            throw new ConfigurationException("No bean with name applicationEventMulticaster");
        }

        pool = multicaster.executor;
    }

    protected void publishEvent(ApplicationEvent event) throws InvocationTargetException, IllegalAccessException {
        for (Listener listener : listeners) {
            if (listener.eventType.equals(event.getClass())) {
                Async asyncAnn = listener.method.getAnnotation(Async.class);
                if (asyncAnn == null) {
                    listener.method.invoke(listener.eventListener, event);
                    continue;
                }

                Runnable runnable = () -> {
                    try {
                        listener.method.invoke(listener.eventListener, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
                pool.execute(runnable);
            }
        }
    }

    public void addListener(Listener eventListener) {
        listeners.add(eventListener);
    }
}
