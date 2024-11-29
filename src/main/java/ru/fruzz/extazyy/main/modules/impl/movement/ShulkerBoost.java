package ru.fruzz.extazyy.main.modules.impl.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;


@ModuleAnnotation(name = "ShulkerBoost", type = CategoryUtil.Movement)
public class ShulkerBoost extends Module {

    int t = 0;
    @EventHandler
    public void packet(TickEvent e) {
        BlockPos playerPos = mc.player.blockPosition();
        t++;
        int radius = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos blockPos = playerPos.offset(x, y, z);
                    BlockState blockState = mc.level.getBlockState(blockPos);
                    if(blockState.getBlock() == Blocks.SHULKER_BOX) {
                        if (t >= 12) {
                            t = 0;
                            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * 1.4, mc.player.getDeltaMovement().y * 3, mc.player.getDeltaMovement().z * 1.40);
                            mc.player.jumpFromGround();
                        }
                    }
                }
            }
        }
    }

}
