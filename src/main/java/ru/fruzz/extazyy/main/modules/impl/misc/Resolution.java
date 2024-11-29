package ru.fruzz.extazyy.main.modules.impl.misc;

import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;

@ModuleAnnotation(name = "Resolution", type = CategoryUtil.Misc)
public class Resolution extends Module {

    public NumberTools width = new NumberTools("X", 16, 1, 20, 0.1f);
    public NumberTools width2 = new NumberTools("Y", 9, 1, 10, 0.1f);

    public Resolution() {
        addSettings(width, width2);
    }



}


