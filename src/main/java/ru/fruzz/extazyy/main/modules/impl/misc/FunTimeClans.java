package ru.fruzz.extazyy.main.modules.impl.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.EventMotion;
import ru.fruzz.extazyy.misc.event.events.impl.RotationEvent;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.misc.util.ClientUtil;
import ru.fruzz.extazyy.misc.util.Mine;


@ModuleAnnotation(name = "ClanUpgrader", type = CategoryUtil.Misc)
public class FunTimeClans extends Module implements Mine {

    @EventHandler
    public void onTick(RotationEvent e) {
            if(mc.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.REDSTONE) {
                BlockPos pos = mc.player.blockPosition();
                BlockPos target = pos.above(-1);
                Vec3 vec3 = new Vec3(target.getX(), target.getY(), target.getZ());
                BlockHitResult result2 = new BlockHitResult(vec3, Direction.UP, target, false);
                mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result2, 1));
                mc.gameMode.startDestroyBlock(pos, Direction.UP);
            } else {
                ClientUtil.sendMessage("Для абуза лвл-а клана необходим редстоун в руке!");
                toggle();
            }
    }


}
