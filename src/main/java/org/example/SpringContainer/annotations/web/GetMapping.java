package org.example.SpringContainer.annotations.web;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@RequestMapping(method = RequestMethod.GET)
public @interface GetMapping {
    String value() default "";
}