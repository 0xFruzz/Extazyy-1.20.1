package ru.fruzz.extazyy.main.commands.impl;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import ru.fruzz.extazyy.main.commands.Command;
import ru.fruzz.extazyy.main.commands.CommandInfo;
import ru.fruzz.extazyy.misc.util.ClientUtil;
import ru.fruzz.extazyy.misc.util.Utils;

@CommandInfo(name = "go", description = "Переносит тебя и предмет в руке на анархию")
public class TransferCommand extends Command {

    @Override
    public void run(String[] args) throws Exception {
        if(args.length < 1) {
            ClientUtil.sendMessage("Использование: .go 202 (Число анархии)");
            return;
        }
        Thread trans = new Thread(() -> {
            try {
                String current = Utils.currentAnarchy();
                String gotoanarchy = args[1];

                Utils.sendChat("/ah dsell 10");
                Thread.sleep(200);

                Utils.sendChat("/an" + gotoanarchy);
                Thread.sleep(1500);
                Utils.sendChat("/ah " + mc.player.getName().getString());
                Thread.sleep(500);
                AbstractContainerScreen.customClick(0, 0, ClickType.QUICK_MOVE);
                Thread.sleep(500);
                mc.player.closeContainer();

                ClientUtil.sendMessage("Перенос с " + current + " на " + gotoanarchy + " выполнен успешно!");

            }catch (Exception e) {
            }
        });
        trans.start();
    }

    @Override
    public void error() {

    }
}
