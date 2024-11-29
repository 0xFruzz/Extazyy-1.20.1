package ru.fruzz.extazyy.main.modules.impl.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.main.modules.tools.imp.MultiBoxTools;
import ru.fruzz.extazyy.misc.util.render.RenderUtil;

import java.awt.*;

@ModuleAnnotation(name = "Xray", type = CategoryUtil.Render)
public class Xray extends Module {

    MultiBoxTools ores = new MultiBoxTools("Подсвечивать",
            new BooleanOption("Алмазы", true),
            new BooleanOption("Незерит", true));

    public Xray() {
        addSettings(ores);
    }

    @EventHandler
    public void render(RenderEvent3D e) {
        BlockPos playerPos = mc.player.blockPosition();
        int radius = 20;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos blockPos = playerPos.offset(x, y, z);
                    BlockState blockState = mc.level.getBlockState(blockPos);

                    if (blockState.getBlock() == Blocks.DIAMOND_ORE) {
                        RenderUtil.drawBox(e.getPoseStack(), new AABB(blockPos), new Color(65, 154, 203, 60).getRGB());
                    }
                    if(blockState.getBlock() == Blocks.ANCIENT_DEBRIS) {
                        RenderUtil.drawBox(e.getPoseStack(), new AABB(blockPos), new Color(47, 2, 9, 102).getRGB());
                    }
                }
            }
        }
    }


}
