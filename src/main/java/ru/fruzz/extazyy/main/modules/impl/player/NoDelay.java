package ru.fruzz.extazyy.main.modules.impl.player;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.BooleanOption;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;

@ModuleAnnotation(name = "NoDelay", type = CategoryUtil.Player)
public class NoDelay extends Module {


    NumberTools delay = new NumberTools("Delay", 0,0,5,1);
    BooleanOption all = new BooleanOption("All", true);
    BooleanOption crystal = new BooleanOption("Crystal", false).setVisible(() -> !all.get());
    BooleanOption blocks = new BooleanOption("Blocks", false).setVisible(() -> !all.get());

    public NoDelay() {
        addSettings(all, crystal, blocks, delay);
    }

    @EventHandler
    public void tick(TickEvent e) {
        if(mc.rightClickDelay > delay.getValue().intValue() && validate()) {
            mc.rightClickDelay = delay.getValue().intValue();
        }
    }

    public boolean validate() {
        assert mc.player != null;
        Item item = mc.player.getMainHandItem().getItem();
        return (item instanceof BlockItem && blocks.get()) || (item == Items.END_CRYSTAL && crystal.get()) || (all.get());
    }

}
