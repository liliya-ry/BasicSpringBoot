package org.example.SpringFramework.SpringContainer.annotations.beans;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Async {
    String value() default "";
}
