package ru.fruzz.extazyy.main.modules.impl.render;

import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent2D;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.util.Utils;

@ModuleAnnotation(name = "BedrockOverlay", type = CategoryUtil.Render)
public class BedrockOverlay extends Module {

    NumberTools tools = new NumberTools("Scale", 15, 15, 50, 1);

    public BedrockOverlay() {
        addSettings(tools);
    }
    @EventHandler
    public void onE(RenderEvent2D e) {
        int w = e.getGuiGraphics().guiWidth();
        Utils.renderEntityInInventoryFollowsMouse(e.getGuiGraphics(),w - 50,50 + tools.getValue().intValue(), tools.getValue().intValue(),0,Utils.mirror((int) mc.player.getXRot()),mc.player);

    }

}
