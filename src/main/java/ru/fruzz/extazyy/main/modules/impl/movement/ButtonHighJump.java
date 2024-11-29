package ru.fruzz.extazyy.main.modules.impl.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.EventMotion;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.misc.util.ClientUtil;
import ru.fruzz.extazyy.misc.util.Utils;


@ModuleAnnotation(name = "ButtonHighJump", type = CategoryUtil.Movement)
public class ButtonHighJump extends Module {
    private BlockPos startPos;
    private boolean isFlying = false;

    public void onEnable() {
        startPos = mc.player.blockPosition();
        isFlying = true;
    }

    @EventHandler
    public void packet(EventMotion e) {
        if (!isFlying) return;
        ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if(stack.toString().contains("button")) {
            BlockPos pos = mc.player.blockPosition();
            BlockPos target = pos.above(-1);
            BlockPos target1 = pos.above(0);

                mc.player.setOnGround(true);

                Vec3 vec3 = new Vec3(target.getX(), target.getY(), target.getZ());
                BlockHitResult result = new BlockHitResult(vec3, Direction.UP, target, false);

                Vec3 vec31 = new Vec3(target1.getX(), target1.getY(), target1.getZ());
                BlockHitResult result2 = new BlockHitResult(vec31, Direction.UP, target1, false);

                mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, 1));
                mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result2, 1));
                mc.player.setDeltaMovement(0, 0.6f, 0);

            if (startPos != null && mc.player.blockPosition().getY() - startPos.getY() >= 3) {
                toggle();
                isFlying = false;
            }
        } else {
            ClientUtil.sendMessage("Для высокого прыжка необходима кнопка в руке!");
            toggle();
        }

    }
}


