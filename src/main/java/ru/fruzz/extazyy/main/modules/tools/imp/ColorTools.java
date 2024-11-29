package ru.fruzz.extazyy.main.modules.tools.imp;


import ru.fruzz.extazyy.main.modules.tools.Tools;

import java.awt.*;
import java.util.function.Supplier;

public class ColorTools extends Tools {
    public int color = 0;

    public ColorTools(String name, int color) {
        super(name);
        this.color = color;
    }

    public int get() {
        return color;
    }

    public Color getColor() {
        return new Color(color);
    }

    public ColorTools setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    @Override
    public SettingType getType() {
        return SettingType.COLOR_SETTING;
    }
}
