package ru.fruzz.extazyy.main.modules.impl.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.util.Mine;

@ModuleAnnotation(name = "HitBox", type = CategoryUtil.Combat)
public class HitBox extends Module implements Mine {


    public NumberTools width = new NumberTools("Size", 0.2f, 0.01f, 0.5f, 0.05f);

    public HitBox() {
        addSettings(width);
    }

    @EventHandler
    public void on3D(RenderEvent3D e) {
        adjustBoundingBoxesForPlayers();
    }

    private void adjustBoundingBoxesForPlayers() {
        for (Player player : Mine.mc.level.players()) {
            if (shouldSkipPlayer(player))
                continue;
            float sizeMultiplier = width.getValue().floatValue() * 2.5F;
            setBoundingBox(player, sizeMultiplier);
        }
    }


    private boolean shouldSkipPlayer(Player player) {
        return player == Mine.mc.player || !player.isAlive();
    }


    private void setBoundingBox(Entity entity, float size) {

        AABB newBoundingBox = calculateBoundingBox(entity, size);
        entity.setBoundingBox(newBoundingBox);
    }


    private AABB calculateBoundingBox(Entity entity, float size) {
        double minX = entity.getX() - size;
        double minY = entity.getBoundingBox().minY;
        double minZ = entity.getZ() - size;
        double maxX = entity.getX() + size;
        double maxY = entity.getBoundingBox().maxY;
        double maxZ = entity.getZ() + size;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

}


