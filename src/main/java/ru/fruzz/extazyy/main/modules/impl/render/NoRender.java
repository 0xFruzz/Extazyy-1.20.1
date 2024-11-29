package ru.fruzz.extazyy.main.modules.impl.render;

import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;

@ModuleAnnotation(name = "NoRender", type = CategoryUtil.Render)
public class NoRender extends Module {

    public BooleanOption fire = new BooleanOption("Fire", true);

    public NoRender() {
        addSettings(fire);
    }

}


