package ru.fruzz.extazyy.misc.api.telegram.commands.impl;

import ru.fruzz.extazyy.misc.api.telegram.commands.ApiCommand;
import ru.fruzz.extazyy.misc.api.telegram.commands.ApiCommandInfo;

@ApiCommandInfo(command = "-test", description = "дадаада, дескрипция по твоей хуйне")
public class TestCommand extends ApiCommand {
    @Override
    public void run(String[] args) throws Exception {
        System.out.println("САНЯ ДАУН А API РАБОТАЕТ");
    }
}
