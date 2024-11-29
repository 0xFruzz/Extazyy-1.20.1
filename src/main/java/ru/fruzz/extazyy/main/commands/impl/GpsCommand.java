package ru.fruzz.extazyy.main.commands.impl;

import net.minecraft.world.phys.Vec3;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.commands.Command;
import ru.fruzz.extazyy.main.commands.CommandInfo;
import ru.fruzz.extazyy.misc.util.ClientUtil;

@CommandInfo(name = "gps", description = "Указывает стрелочку на указанную точку")
public class GpsCommand extends Command {

    public boolean enabled;
    public static Vec3 vector3d;

    @Override
    public void run(String[] args) throws Exception {
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("off")) {
                ClientUtil.sendMessage("Навигатор выключен!");
                enabled = false;
                vector3d = null;
                return;
            }
            if (args.length == 3) {
                int x = Integer.parseInt(args[1]);
                int y = Integer.parseInt(args[2]);
                enabled = true;
                vector3d = new Vec3(x, 0.0, y);
                ClientUtil.sendMessage("Навигатор включен! Координаты " + x + " " + y);
                if(!Extazyy.getModuleManager().hud.settings.get("GPS") || !Extazyy.getModuleManager().hud.state) {
                    ClientUtil.sendMessage("Внимание! Для стабильной работы навигатора необходим включенный модуль HUD с включенным параметром GPS");
                }
            }
        } else {
            this.error();
        }
    }


    @Override
    public void error() {
        ClientUtil.sendMessage("Ошибка в аргументах команды"  + ":");
        ClientUtil.sendMessage(".gps <" + "x, z"  + ">");
        ClientUtil.sendMessage(".gps "  + "<"  + "off" + ">");
    }


}
