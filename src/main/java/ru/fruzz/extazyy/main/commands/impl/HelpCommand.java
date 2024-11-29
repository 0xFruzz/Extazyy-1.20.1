package ru.fruzz.extazyy.main.commands.impl;

import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.commands.Command;
import ru.fruzz.extazyy.main.commands.CommandInfo;
import ru.fruzz.extazyy.misc.util.ClientUtil;

@CommandInfo(name = "help", description = "Показывает это меню!")
public class HelpCommand extends Command {


    @Override
    public void run(String[] args) throws Exception {
        for (Command cmd : Extazyy.commandmgr.getCommands()) {
            ClientUtil.sendMessage("." + cmd.command + " - " + cmd.description);
        }
    }

    @Override
    public void error() {
        ClientUtil.sendMessage("Error");
    }
}
