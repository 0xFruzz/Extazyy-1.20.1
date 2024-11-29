package ru.fruzz.extazyy.misc.api.telegram.commands;


import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.commands.Command;
import ru.fruzz.extazyy.misc.api.telegram.commands.impl.TestCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiCommandManager {

    public List<ApiCommand> commandList = new ArrayList<>();

    public void init() {
        commandList.addAll(Arrays.asList(
                new TestCommand()
        ));
    }

    public void execute(String message) {
        String[] args = message.split(" ");
        for (ApiCommand command : getCommands()) {
            if(!args[0].equals(command.command)) continue;
            try {
                command.run(args);
            }catch (Exception ignored) {
            }
        }
    }


    public List<ApiCommand> getCommands() {
        return commandList;
    }
}
