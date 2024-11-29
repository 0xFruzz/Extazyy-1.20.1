package ru.fruzz.extazyy.misc.api.telegram.commands;

import ru.fruzz.extazyy.main.commands.CommandInfo;
import ru.fruzz.extazyy.misc.util.Mine;

public abstract class ApiCommand implements Mine {

    public final String command, description;
    public ApiCommand() {
        command = this.getClass().getAnnotation(ApiCommandInfo.class).command();
        description = this.getClass().getAnnotation(ApiCommandInfo.class).description();
    }

    public abstract void run(String[] args) throws Exception;

}

