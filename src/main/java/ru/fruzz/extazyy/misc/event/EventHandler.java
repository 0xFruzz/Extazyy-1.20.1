package ru.fruzz.extazyy.misc.event;


import ru.fruzz.extazyy.misc.event.types.Priority;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

    byte value() default Priority.MEDIUM;
}
