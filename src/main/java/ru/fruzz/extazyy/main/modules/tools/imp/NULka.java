package ru.fruzz.extazyy.main.modules.tools.imp;

import lombok.Getter;
import lombok.Setter;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleMessage;
import ru.fruzz.extazyy.main.modules.tools.Tools;

import java.util.function.Supplier;

@Getter
@Setter
public class NULka extends Tools {

    ModuleMessage message;

    public NULka(String name,  ModuleMessage message) {
        super(name);
        this.message = message;
    }
    public NULka setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    @Override
    public SettingType getType() {
        return SettingType.NULL_OPTION;
    }
}
