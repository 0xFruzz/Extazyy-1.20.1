package ru.fruzz.extazyy.main.modules.impl.misc;

import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;

@ModuleAnnotation(name = "CameraOffset", type = CategoryUtil.Misc)
public class CameraOffset extends Module {
    public final NumberTools x = new NumberTools("X", 0,-10,10,0.1f);
    public final NumberTools y = new NumberTools("Y", 0,-10,10,0.1f);
    public final NumberTools z = new NumberTools("Z", 0,-10,10,0.1f);

    public CameraOffset() {
        addSettings(x,y,z);
    }
}
