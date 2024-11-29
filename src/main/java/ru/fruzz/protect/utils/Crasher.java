package ru.fruzz.protect.utils;

import net.minecraft.server.Bootstrap;
import ru.fruzz.extazyy.misc.event.EventManager;

public class Crasher {

    public void crash() {
        Bootstrap.realStdoutPrintln("#@?@# Game crashed! Sosi huyaku blya. #@?@#");
        EventManager.cleanMap(false);
        System.exit(-2);
    }

    public static Crasher getCrasher() {
        return new Crasher();
    }

}
