package ru.fruzz.extazyy.misc.font;

import ru.fruzz.extazyy.misc.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class FontRenderers {
    public static FontRenderer mntsb14;
    public static FontRenderer mntsb16;
    public static FontRenderer msLight14;
    public static FontRenderer msLight16;
    public static FontRenderer msSemi12;
    public static FontRenderer msSemi13;
    public static FontRenderer msSemi14;
    public static FontRenderer msSemi16;
    public static FontRenderer msSemi32;
    public static FontRenderer msSemi24;
    public static FontRenderer extazyy16;
    public static FontRenderer extazyy18;
    public static FontRenderer extazyy24;

    public static FontRenderer umbrellagui24;
    public static FontRenderer umbrellagui30;
    public static FontRenderer umbrellagui17;
    public static FontRenderer umbrellagui16;
    public static FontRenderer umbrellagui15;
    public static FontRenderer umbrellagui20;
    public static FontRenderer umbrellagui22;

    public static FontRenderer umbrellatext14;
    public static FontRenderer umbrellatext15;
    public static FontRenderer umbrellatext16;
    public static FontRenderer umbrellatext17;
    public static FontRenderer umbrellatext18;

    public static FontRenderer generalgui14;
    public static FontRenderer generalgui15;
    public static FontRenderer generalgui16;
    public static FontRenderer generalgui17;
    public static FontRenderer generalgui18;

    public static FontRenderer nuricon18;
    public static FontRenderer nuricon16;
    public static FontRenderer nuricon20;


    public static FontRenderer glyphter;


    public static FontRenderer music12;
    public static FontRenderer music13;
    public static FontRenderer music14;
    public static FontRenderer music15;
    public static FontRenderer music16;
    public static FontRenderer music17;
    public static FontRenderer music10;

    
    public @NotNull FontRenderer create(float size, String name) throws IOException, FontFormatException {
        return new FontRenderer(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Utils.getCRStream("font/" + name + ".ttf"))).deriveFont(Font.PLAIN, size / 2f), size / 2f);
    }

    public void init() {
        try {
            nuricon18 = create(18, "nur-icon");

            nuricon20 = create(20, "nur-icon");
            nuricon16 = create(16, "nur-icon");
            msLight14 = create(14, "Montserrat-Light");
            msLight16 = create(16, "Montserrat-Light");
            mntsb14 = create(14, "mntsb");
            mntsb16 = create(16, "mntsb");
            msSemi12 = create(12, "Montserrat-SemiBold");
            msSemi13 = create(13, "Montserrat-SemiBold");
            msSemi14 = create(14, "Montserrat-SemiBold");
            msSemi16 = create(16, "Montserrat-SemiBold");
            msSemi24 = create(24, "Montserrat-SemiBold");
            msSemi32 = create(32, "Montserrat-SemiBold");
            extazyy16 = create(16, "ExtazyyIcons");
            extazyy18 = create(18, "ExtazyyIcons");
            extazyy24 = create(24, "ExtazyyIcons");
            umbrellagui15 = create(15, "UmbrellaGui");
            umbrellagui16 = create(16, "UmbrellaGui");
            umbrellagui17 = create(17, "UmbrellaGui");
            umbrellagui30 = create(30, "UmbrellaGui");


            umbrellagui20 = create(20, "UmbrellaGui");
            umbrellagui22 = create(22, "UmbrellaGui");
            umbrellagui24 = create(24, "UmbrellaGui");

            umbrellatext14 = create(14, "umbrella");
            umbrellatext15 = create(15, "umbrella");
            umbrellatext16 = create(16, "umbrella");
            umbrellatext17 = create(17, "umbrella");
            umbrellatext18 = create(18, "umbrella");

            generalgui14 = create(14, "GeneralGui");
            generalgui15 = create(15, "GeneralGui");
            generalgui16 = create(16, "GeneralGui");
            generalgui17 = create(17, "GeneralGui");
            generalgui18 = create(18, "GeneralGui");


            glyphter = create(15, "Glyphter");

            music12 = create(12, "customicons");
            music13 = create(13, "customicons");
            music14 = create(14, "customicons");
            music15 = create(15, "customicons");
            music16 = create(16, "customicons");
            music17 = create(17, "customicons");
            music10 = create(10, "customicons");


        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }
}
