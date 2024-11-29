package ru.fruzz.extazyy.main.modules.impl.combat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Items;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.main.modules.tools.imp.ModeTools;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.misc.util.Utils;


@ModuleAnnotation(name = "AutoTotem", type = CategoryUtil.Combat)
public class AutoTotem extends Module {

    NumberTools healths = new NumberTools("При хп", 5, 0, 20, 0.5f);
    BooleanOption option = new BooleanOption("Учитывать гепл", true);
    ModeTools modeTools = new ModeTools("Мод переключения", "Стандарт", "Стандарт", "ViaVersion");
    public AutoTotem() {
        addSettings(healths, modeTools, option);
    }


    private int swapBackSlot = -1;

    @EventHandler
    public void onUpdate(TickEvent e) {
        int slot = Utils.findItemSlot(Items.TOTEM_OF_UNDYING, false);
        boolean totemInHand = mc.player.getItemInHand(InteractionHand.OFF_HAND).getItem().equals(Items.TOTEM_OF_UNDYING);
        boolean handNotNull = !(mc.player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof AirItem);
        if (slot >= 0 && condition()) {
            if (!totemInHand) {
                swapItem(slot);
                if (handNotNull) {
                    if (swapBackSlot == -1) swapBackSlot = slot;
                }
            }
        }
    }

    private boolean condition() {
        float health = mc.player.getHealth();
        if (option.get()) {
            health += mc.player.getAbsorptionAmount();
        }

        if (healths.getValue().floatValue() >= health) {
            return true;
        }

        return false;
    }

    public void swapItem(int slot) {
        if (modeTools.is("Стандарт")) {
            mc.gameMode.handleInventoryMouseClick(0, slot, 1, ClickType.PICKUP, mc.player);
            mc.gameMode.handleInventoryMouseClick(0, 45, 1, ClickType.PICKUP, mc.player);
        }
        if (modeTools.is("ViaVersion")) {
          //  mc.gameMode.handleInventoryMouseClick(mc.gameMode..syncId, slot, 40, SlotActionType.SWAP, mc.player);
            //IHolder.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }
    }
}
