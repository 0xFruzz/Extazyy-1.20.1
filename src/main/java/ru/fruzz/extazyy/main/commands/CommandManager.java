package ru.fruzz.extazyy.main.commands;






import ru.fruzz.extazyy.main.commands.impl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {
    public List<Command> commandList = new ArrayList<>();

    public GpsCommand gpsCommand;

    public void init() {
        commandList.addAll(Arrays.asList(
                new HelpCommand(),
                new RCTCommand(),
                new FriendCommand(),
                new TransferCommand(),
                gpsCommand = new GpsCommand()
        ));
    }




    public List<Command> getCommands() {
        return commandList;
    }
}
