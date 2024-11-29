package ru.fruzz.extazyy.main.modules.impl.player;

import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.misc.util.math.TimerUtil;


@ModuleAnnotation(name = "ChestStealer", type = CategoryUtil.Player)
public class ChestStealer extends Module {

    BooleanOption close = new BooleanOption("Закрывать", false);

    NumberTools stealDelay = new NumberTools("Задержка", 100,0,1000,1);

    TimerUtil delay = new TimerUtil();

    public ChestStealer() {
        addSettings(close, stealDelay);
    }

    @EventHandler
    public void chest(RenderEvent3D e) {
        if (mc.player.containerMenu instanceof ChestMenu chest) {
            for (int i = 0; i < chest.getContainer().getContainerSize(); i++) {
                Slot slot = chest.getSlot(i);
                if (slot.hasItem() && delay.hasTimeElapsed(stealDelay.getValue().longValue()) && !(mc.screen.getTitle().getString().contains("Аукцион") || mc.screen.getTitle().getString().contains("покупки"))) {
                    mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);

                }
            }
            if (isContainerEmpty(chest) && close.get())
                mc.player.closeContainer();
        }

    }

    private boolean isContainerEmpty(ChestMenu container) {
        for (int i = 0; i < (container.getContainer().getContainerSize() == 90 ? 54 : 27); i++)
            if (container.getSlot(i).hasItem()) return false;
        return true;
    }


}
