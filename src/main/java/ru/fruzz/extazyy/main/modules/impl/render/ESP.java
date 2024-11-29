package ru.fruzz.extazyy.main.modules.impl.render;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.main.modules.tools.imp.ModeTools;
import ru.fruzz.extazyy.main.modules.tools.imp.MultiBoxTools;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.render.RenderUtil;

import java.awt.*;


@ModuleAnnotation(name = "ESP", type = CategoryUtil.Render)
public class ESP extends Module {

    ModeTools tools = new ModeTools("Тип", "Box", "Box");

    @EventHandler
    public void render3D(RenderEvent3D e) {
        for (Entity entity : mc.level.getEntities().getAll()) {
            if (entity instanceof Player && !(entity instanceof LocalPlayer)) {
                double interpolatedX = entity.xOld + (entity.getX() - entity.xOld) * e.getPartialTicks();
                double interpolatedY = entity.yOld + (entity.getY() - entity.yOld) * e.getPartialTicks();
                double interpolatedZ = entity.zOld + (entity.getZ() - entity.zOld) * e.getPartialTicks();

                AABB aabb = makeBoundingBox(entity, interpolatedX, interpolatedY, interpolatedZ);
                if(tools.is("Box")) {
                    RenderUtil.drawBox(e.getPoseStack(), aabb, new Color(ColorUtil.getRed(Extazyy.getThemesUtil().getCurrentStyle().getColorLowSpeed(0)), ColorUtil.getGreen(Extazyy.getThemesUtil().getCurrentStyle().getColorLowSpeed(0)), ColorUtil.getBlue(Extazyy.getThemesUtil().getCurrentStyle().getColorLowSpeed(0)), 100).getRGB());
                }
            }
        }
    }


    public AABB makeBoundingBox(Entity entity ,double pX, double pY, double pZ) {
        float f = entity.getBbWidth() / 2.0F;
        float f1 = entity.getBbHeight();
        return new AABB(pX - (double)f, pY, pZ - (double)f, pX + (double)f, pY + (double)f1, pZ + (double)f);
    }


}
