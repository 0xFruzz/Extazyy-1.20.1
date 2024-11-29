package ru.fruzz.extazyy.main.modules.impl.render;

import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.EventPacket;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.*;
import ru.fruzz.extazyy.misc.util.Mine;

import ru.fruzz.extazyy.main.modules.ModuleApi.Module;

@ModuleAnnotation(name = "Custom World", type = CategoryUtil.Render)
public class CustomWorld extends Module {

    public MultiBoxTools modes = new MultiBoxTools("Изменять",
            new BooleanOption("Время", false),
            new BooleanOption("Туман", false));

    private ModeTools timeOfDay = new ModeTools("Время суток", "Ночь", "День", "Закат", "Рассвет", "Ночь", "Полночь", "Полдень").setVisible(() -> modes.get(0));
    public BooleanOption theme = new BooleanOption("Привязать к теме", true).setVisible(() -> modes.get(1));
    public ColorTools colorFog = new ColorTools("Цвет тумана", -1).setVisible(() -> modes.get(1)).setVisible(() -> !theme.get());
    public NumberTools distanceFog = new NumberTools("Дальность тумана", 30.0F, 1f, 90.0F, 1).setVisible(() -> modes.get(1));


    public CustomWorld() {
        addSettings(modes, timeOfDay, theme, distanceFog, colorFog);
    }


    @EventHandler
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof ClientboundSetTimePacket) {
            if (modes.get(0)) {
                e.stop();
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (modes.get(0)) {
            float time = 0;
            switch (timeOfDay.get()) {
                case "День" -> time = 1000;
                case "Закат" -> time = 12000;
                case "Рассвет" -> time = 23000;
                case "Ночь" -> time = 13000;
                case "Полночь" -> time = 18000;
                case "Полдень" -> time = 6000;
            }
            Mine.mc.level.setDayTime((long) time);
        }
    }
}