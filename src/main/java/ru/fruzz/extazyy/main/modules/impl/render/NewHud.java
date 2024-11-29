package ru.fruzz.extazyy.main.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.drag.Dragging;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent2D;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;


@ModuleAnnotation(name = "NewHud",type = CategoryUtil.Render)
public class NewHud extends Module {

    //Constants
    Dragging keybinds = Extazyy.createDrag(this, "KeyBindsNewHud", 150,100);

    @EventHandler
    public void render2D(RenderEvent2D e) {
        watermark(e.getGuiGraphics().pose());
      //  keybinds(e.getGuiGraphics().pose());
    }

    private void watermark(PoseStack stack) {
        float x = 10;
        float y = 10;
        float width = 157.5f ;
        float height = 22;

        DrawHelper.rectRGB(stack, x,y,width,height,7, new Color(19, 21, 25, 255).getRGB(), new Color(19, 21, 25,255).getRGB(), new Color(19, 19, 20,255).getRGB(), new Color(19, 19, 20,255).getRGB());

        //  FontRenderers.umbrellagui24.drawString(stack, String.valueOf('J'), x + 6f, y + 7.5f, new Color(163, 155, 214, 255).getRGB());
    }

    private void keybinds(PoseStack stack) {
        float x = keybinds.getX();
        float y = keybinds.getY();
        float width = 80;
        float height = 60;

        DrawHelper.rectRGB(stack, x,y,width,height,2, new Color(32, 31, 38, 255).getRGB(), new Color(32, 31, 38,255).getRGB(), new Color(18, 18, 18,255).getRGB(), new Color(18, 18, 18,255).getRGB());

        keybinds.setHeight(height);
        keybinds.setWidth(width);
    }


}
