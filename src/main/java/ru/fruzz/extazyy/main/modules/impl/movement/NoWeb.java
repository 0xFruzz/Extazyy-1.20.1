package ru.fruzz.extazyy.main.modules.impl.movement;

import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.misc.util.Utils;


@ModuleAnnotation(name = "NoWeb", type = CategoryUtil.Movement)
public class NoWeb extends Module {

        float speed = 0.6f;
        @EventHandler
        public void onTick(TickEvent e) {
            if(Utils.isInWeb()) {
                final double[] dir = Utils.forward(speed);
                mc.player.setDeltaMovement(dir[0], 0, dir[1]);
                if (mc.options.keyJump.isDown())
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, speed, 0));
                if (mc.options.keyShift.isDown())
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, -speed, 0));
            }
        }


}
