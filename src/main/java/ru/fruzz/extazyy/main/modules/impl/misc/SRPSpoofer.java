package ru.fruzz.extazyy.main.modules.impl.misc;

import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.EventPacket;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;


@ModuleAnnotation(name = "RPSpoofer", type = CategoryUtil.Misc)
public class SRPSpoofer extends Module {




    @EventHandler
    public void packet(EventPacket e) {
        if(e.getPacket() instanceof ServerboundResourcePackPacket) {
            mc.getConnection().send(new ServerboundResourcePackPacket(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
            e.stop();
        }
    }

   

}
