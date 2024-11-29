package ru.fruzz.extazyy.main.themes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThemesUtil {
    public List<Theme> themes = new ArrayList<>();
    private Theme currentTheme = null;

    public void init() {
        themes.addAll(Arrays.asList(
                new Theme("Night", HexColor.toColor("#2980B9"), HexColor.toColor("#2C3E50")),
                new Theme("Electric", HexColor.toColor("#4776E6"), HexColor.toColor("#8E54E9")),
                new Theme("Bloody Mary", HexColor.toColor("#FF512F"), HexColor.toColor("#DD2476")),
                new Theme("Monte Carlo", HexColor.toColor("#CC95C0"), HexColor.toColor("#DBD4B4")),
                new Theme("Meriada", HexColor.toColor("#00F260"), HexColor.toColor("#0575E6")),
                new Theme("Orange Fun", HexColor.toColor("#FC4A1A"), HexColor.toColor("#F7B733")),
                new Theme("Celestial", HexColor.toColor("#C33764"), HexColor.toColor("#1D2671")),
                new Theme("Lollipop", HexColor.toColor("#F69ABF"), HexColor.toColor("#EA5455")),
                new Theme("Doup", HexColor.toColor("#AA076B"), HexColor.toColor("#61045F")),
                new Theme("Azure", HexColor.toColor("#EF32D9"), HexColor.toColor("#89FFFD")),
                new Theme("Magic", HexColor.toColor("#5A189A"), HexColor.toColor("#FAA307")),
                new Theme("Flare", HexColor.toColor("#FF6F61"), HexColor.toColor("#6B5B95")),
                new Theme("Falling", HexColor.toColor("#FEB47B"), HexColor.toColor("#FF7E5F")),
                new Theme("Moonlight", HexColor.toColor("#6A85B6"), HexColor.toColor("#B7C0EE"))
                )
        );
        currentTheme = themes.get(1);
    }


    public void setCurrentStyle(Theme theme) {
        currentTheme = theme;
    }


    public Theme getCurrentStyle() {
        return currentTheme;
    }

    public void setCurrentThemeByName(String name) {
        for (Theme theme : themes) {
            if (theme.getName().equalsIgnoreCase(name)) {
                setCurrentStyle(theme);
            }
        }
    }


}
