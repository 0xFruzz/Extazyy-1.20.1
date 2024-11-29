package ru.fruzz.extazyy.main.modules.impl.movement;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.misc.util.Mine;

@ModuleAnnotation(name = "EntitySpeed", type = CategoryUtil.Movement)
public class StrafeSpeed extends Module { //открытие тела класса

    @EventHandler
    public void tick(TickEvent e) {
            if (mc.player.isSprinting() && !mc.player.onGround()) {
                for (Player entity : mc.level.players()) {
                    if(entity != mc.player) {
                        AABB yourBB = mc.player.getBoundingBox();
                        AABB ent = entity.getBoundingBox();
                        if (yourBB.intersects(ent)) {
                            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * 1.3200, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z * 1.3200);
                        }
                    }
                }
            }

    }
}