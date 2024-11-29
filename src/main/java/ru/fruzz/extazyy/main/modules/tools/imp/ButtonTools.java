package ru.fruzz.extazyy.main.modules.tools.imp;

import ru.fruzz.extazyy.main.modules.tools.Tools;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
public class ButtonTools extends Tools {

    private Runnable run;

    public ButtonTools(String name, Runnable run) {
        super(name);
        this.run = run;
    }
    public ButtonTools setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    @Override
    public SettingType getType() {
        return SettingType.BUTTON_SETTING;
    }
}
