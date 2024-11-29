package ru.fruzz.extazyy.main.modules.impl.movement;

import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.EventMotion;


@ModuleAnnotation(name = "FunTest", type = CategoryUtil.Movement)
public class FunTest extends Module {

    @EventHandler
    public void event(EventMotion e) {

        mc.player.setOnGround(true);
        mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, offset(), mc.player.getDeltaMovement().z);

    }

    private float offset() {
        if(mc.player.isShiftKeyDown()) {
            return -0.1f;
        } else if (mc.options.keyJump.isDown()) {
            return (float) (mc.player.getDeltaMovement().y +0.1f);
        } else {
            return 0;
        }
    }

}
