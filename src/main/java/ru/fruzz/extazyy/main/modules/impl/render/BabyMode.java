package ru.fruzz.extazyy.main.modules.impl.render;

import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;

@ModuleAnnotation(name = "BabyMode", type = CategoryUtil.Render, setting = false)
public class BabyMode extends Module {

    public final BooleanOption here = new BooleanOption("Только себя.", false);

    public BabyMode() {
        addSettings(here);
    }
}
