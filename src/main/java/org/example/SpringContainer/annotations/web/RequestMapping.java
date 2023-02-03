package org.example.SpringContainer.annotations.web;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestMapping {
    String value() default "";
    RequestMethod method() default RequestMethod.GET;
}
