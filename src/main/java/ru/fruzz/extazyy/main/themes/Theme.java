package ru.fruzz.extazyy.main.themes;


import lombok.Getter;
import ru.fruzz.extazyy.misc.util.color.ColorUtils;

public class Theme {
    @Getter
    public String name;
    public int[] colors;

    public Theme(String name, int... colors) {
        this.name = name;
        this.colors = colors;
    }


    public int getColor(int index) {
        return ColorUtils.gradient(25,
                index, colors);
    }

    public int getColorLowSpeed(int index) {
        return ColorUtils.gradient(50,
                index, colors);
    }



}