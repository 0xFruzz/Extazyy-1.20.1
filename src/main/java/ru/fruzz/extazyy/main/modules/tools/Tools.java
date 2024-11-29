package ru.fruzz.extazyy.main.modules.tools;

import lombok.Getter;

import java.awt.*;
import java.util.function.Supplier;

@Getter
public abstract class Tools {
    private final String name;
    public Supplier<Boolean> visible = () -> true;
    public Color color = Color.WHITE;
    public abstract SettingType getType();

    public Tools(String name) {
        this.name = name;
    }


    public Boolean visible() {
        return visible.get();
    }

    public enum SettingType {
        NULL_OPTION,
        BOOLEAN_OPTION,
        NUMBER_SETTING,
        MODE_SETTING,
        COLOR_SETTING,
        MULTI_BOX_SETTING,
        BIND_SETTING,
        BUTTON_SETTING,
        TEXT_SETTING,
        SETTING_RENDER
    }
}