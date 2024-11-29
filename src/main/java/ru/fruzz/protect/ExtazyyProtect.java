package ru.fruzz.protect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.info.UserInfo;

public class ExtazyyProtect {

    private UserInfo userInfo = null;
    private final Logger LOGGER = LogManager.getLogger();

    public void dontCrackMe() {
        if (userInfo != null) {
            //BAN | Crash
        }
        if(!System.getProperty("java.vm.name").equals("ExtazyyVM")) {
            //BAN | CRASH
        }
        if(!System.getProperty("java.version").equals("ExtazyyVM")) {
            //BAN | CRASH
        }
        //Добавить проверку существует ли хеш

    }




    public static ExtazyyProtect getInstance() {
        return new ExtazyyProtect();
    }

}
