package ru.fruzz.extazyy.main.modules.tools.imp;

import ru.fruzz.extazyy.main.modules.tools.Tools;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
public class BindTools extends Tools {

    private int key;

    public BindTools(String name, int defaultKey) {
        super(name);
        key = defaultKey;
    }
    public BindTools setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    @Override
    public SettingType getType() {
        return SettingType.BIND_SETTING;
    }
}
