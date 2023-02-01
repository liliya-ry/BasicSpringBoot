package org.example.SpringContainer.annotations.beans;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Async {
    String value() default "";
}
