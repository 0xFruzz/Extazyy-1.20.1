package ru.fruzz.extazyy.main.modules.impl.unused;

import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.EventPacket;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.*;


import ru.fruzz.extazyy.misc.util.color.ColorUtil;


@ModuleAnnotation(name = "TestRender", type = CategoryUtil.Misc)
public class TestModule extends Module {


    public BooleanOption booleanOption = new BooleanOption("Передоз?", true);
    public NumberTools width = new NumberTools("Мама клуши", 1488, 1, 2000, 0.1f);
    public ColorTools color = new ColorTools("Цвет", ColorUtil.toRGBA(255, 255, 255, 255));
    BindTools bind = new BindTools("Bind", 0);
    public ModeTools tool = new ModeTools("Тест", "Тест", "Тест22", "Test33");
    public final MultiBoxTools settings = new MultiBoxTools("Настройки",
            new BooleanOption("Только критами", true),
            new BooleanOption("Корректор", true),
            new BooleanOption("Отжимать щит", true),
            new BooleanOption("Ломать щит", true),
            new BooleanOption("Таргет ЕСП", true)

    );

    public TextTools text = new TextTools("Value", "123123123");



    public TestModule() {
        addSettings(booleanOption, bind, width, color,tool,settings,text);

    }



    @EventHandler
    public void onpacket(EventPacket e) {

    }







}



