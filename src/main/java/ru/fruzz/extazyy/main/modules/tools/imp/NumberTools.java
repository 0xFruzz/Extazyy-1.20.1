package ru.fruzz.extazyy.main.modules.tools.imp;



import ru.fruzz.extazyy.main.modules.tools.Tools;
import lombok.Getter;
import net.minecraft.util.Mth;


import java.util.function.Supplier;

public class NumberTools extends Tools {
    private float value;
    @Getter
    private final float min;
    @Getter
    private final float max;
    @Getter
    private final float increment;


    public NumberTools(String name, float value, float min, float max, float increment) {
        super(name);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;

    }

    public NumberTools setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    public Number getValue() {
        return Mth.clamp(value, getMin(), getMax());
    }

    public void setValue(float value) {
        this.value = Mth.clamp(value, getMin(), getMax());
    }

    @Override
    public SettingType getType() {
        return SettingType.NUMBER_SETTING;
    }
}
