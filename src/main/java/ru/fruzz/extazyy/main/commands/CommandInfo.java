package ru.fruzz.extazyy.main.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String name();
    String description();
}
