package ru.fruzz.extazyy.main.commands;


import ru.fruzz.extazyy.misc.util.Mine;

public abstract class Command implements Mine {
    public final String command, description;
    public Command() {
        command = this.getClass().getAnnotation(CommandInfo.class).name();
        description = this.getClass().getAnnotation(CommandInfo.class).description();
    }

    public abstract void run(String[] args) throws Exception;
    public abstract void error();


}
