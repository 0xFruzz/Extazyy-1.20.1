package ru.fruzz.extazyy.main.modules.tools.imp;



import ru.fruzz.extazyy.main.modules.tools.Tools;

import java.util.function.Supplier;

public class TextTools extends Tools {
    public String text;

    public TextTools(String name, String text) {
        super(name);
       this.text = text;
    }

    public String get() {
        return text;
    }

    public TextTools setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    @Override
    public SettingType getType() {
        return SettingType.TEXT_SETTING;
    }
}
