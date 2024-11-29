package ru.fruzz.extazyy.main.commands.impl;

import ru.fruzz.extazyy.main.commands.Command;
import ru.fruzz.extazyy.main.commands.CommandInfo;
import ru.fruzz.extazyy.misc.util.Utils;


@CommandInfo(name = "rct", description = "Перезаходит на анку фт")
public class RCTCommand extends Command {
    @Override
    public void run(String[] args) throws Exception {

            Thread thread = new Thread(() -> {
                assert mc.player != null;

                String an = Utils.currentAnarchy();
                long ms = 900;

                mc.player.connection.sendChat("/hub");

                try {
                    Thread.sleep(ms);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                mc.player.connection.sendChat("/an" + an);
            });
            thread.start();


    }

    @Override
    public void error() {

    }
}
