package org.example.SpringContainer.events;

public abstract class ApplicationEvent {
    Object source;

    protected ApplicationEvent(Object source) {
        this.source = source;
    }
}
