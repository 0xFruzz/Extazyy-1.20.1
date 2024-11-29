package ru.fruzz.extazyy.main.modules.impl.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Player;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.impl.combat.AntiBot;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.main.modules.tools.imp.ModeTools;
import ru.fruzz.extazyy.main.modules.tools.imp.MultiBoxTools;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.misc.util.Utils;

@ModuleAnnotation(name = "AutoLeave", type = CategoryUtil.Misc)
public class AutoLeave extends Module {

    MultiBoxTools leaveas = new MultiBoxTools("Ливать от",
            new BooleanOption("Игроков", false),
            new BooleanOption("Смерти", true)
            );

    NumberTools distance = new NumberTools("Дистанция лива", 5, 2,30,1).setVisible(() -> leaveas.get("Игроков"));

    ModeTools type = new ModeTools("Тип лива", "/spawn", "/spawn");

    public AutoLeave() {
        addSettings(leaveas, distance, type);
    }

    @EventHandler
    public void check(TickEvent e) {
        isLeave();
    }

    private void isLeave() {
        mc.level.players().stream().filter(this::isValidPlayer)
                .findFirst()
                .ifPresent(this::action);
    }

    private void action(Player player) {
        if(type.is("/spawn")) {
            Utils.sendChat("/spawn");
            toggle();
        }
    }

    private boolean isValidPlayer(Player player) {
        return player.isAlive()
                && player.getHealth() > 0.0f
                && player.distanceToSqr(mc.player) <= distance.getValue().floatValue()
                && player != mc.player
                && !Extazyy.getModuleManager().getAntiBot().isBot(player);
    }

}
