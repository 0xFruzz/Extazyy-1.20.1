package ru.fruzz.extazyy.main.modules.tools.imp;


import ru.fruzz.extazyy.main.modules.tools.Tools;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.function.Supplier;

@Getter
public class ModeTools extends Tools {
    @Setter
    private int index;
    public String[] modes;


    public ModeTools(String name, String current, String... modes) {
        super(name);
        this.modes = modes;
        this.index = Arrays.asList(modes).indexOf(current);
    }

    public boolean is(String mode) {
        return get().equals(mode);
    }

    public String get() {
        try {
            if (index < 0 || index >= modes.length) {
                return modes[0];
            }
            return modes[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "ERROR";
        }
    }

    public void set(String mode) {
        this.index = Arrays.asList(modes).indexOf(mode);
    }

    public void set(int mode) {
        this.index = mode;
    }

    public ModeTools setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    @Override
    public SettingType getType() {
        return SettingType.MODE_SETTING;
    }
}
