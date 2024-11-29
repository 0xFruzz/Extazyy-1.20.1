package ru.fruzz.extazyy.misc.api.telegram.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ApiCommandInfo {
    String command();
    String description();
}
