package ru.fruzz.extazyy.main.modules.ModuleApi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ModuleAnnotation {

    String name();

    String desc() default "";

    boolean risk() default false;

    boolean setting() default false;

    String icon() default "";

    int key() default 0;

    CategoryUtil type();
}
