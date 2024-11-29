package ru.fruzz.extazyy.main.modules.impl.render;

import net.minecraft.world.entity.player.Player;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent2D;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

@ModuleAnnotation(name = "Triangles", type = CategoryUtil.Render)
public class Triangles extends Module {

    @EventHandler
    public void render2d(RenderEvent2D e) {
        for (Player ent : mc.level.players()) {
            if (mc.player.equals(ent)) continue;
            DrawHelper.drawArrow(e.getGuiGraphics().pose(), ent, (float) ent.getX(), (float) ent.getZ());
        }
    }

}
