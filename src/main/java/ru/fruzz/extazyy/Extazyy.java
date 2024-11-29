package ru.fruzz.extazyy;


import lombok.Getter;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import ru.fruzz.extazyy.main.commands.CommandManager;
import ru.fruzz.extazyy.main.drag.DragManager;
import ru.fruzz.extazyy.main.drag.Dragging;
import ru.fruzz.extazyy.misc.api.telegram.TelegramApi;
import ru.fruzz.extazyy.misc.api.telegram.commands.ApiCommandManager;
import ru.fruzz.extazyy.misc.configs.ConfigManager;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.info.BuildInfo;
import ru.fruzz.extazyy.info.Discord;
import ru.fruzz.extazyy.info.UserInfo;
import ru.fruzz.extazyy.main.friends.FriendManager;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;

import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleManager;
import ru.fruzz.extazyy.main.themes.ThemesUtil;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import ru.fruzz.extazyy.misc.ui.clickgui.ClickGui;
import ru.fruzz.extazyy.misc.ui.themeui.ThemeGui;
import ru.fruzz.extazyy.misc.ui.umbrellagui.UmbrellaGui;
import ru.fruzz.extazyy.misc.util.render.dangertech.blur.DrawShader;
import ru.fruzz.extazyy.misc.util.gif.Gif;

@Getter
public class Extazyy {

    /*
    Туда-сюда и я $3lfc0d3r, я легко тапаю пасты
    Я всегда на югейме, меня комару не запастит 💉
    У меня деньги на сайте, мама тапнула за карту
    Я могу послать тя нахуй, ведь я дефолтный пастер ✌️

    Я могу послать тя нахуй, ведь дефолтный пастер ✌️

    by hippy1337
     */

    public static Minecraft mc = Minecraft.getInstance();
    @Getter
    public static Extazyy instance;
    @Getter
    public static ModuleManager moduleManager;
    @Getter
    public static FontRenderers font;
    @Getter
    public static ThemesUtil themesUtil;
    @Getter
    public static CommandManager commandmgr;
    @Getter
    public static FriendManager friendmgr;
    @Getter
    public static ConfigManager configmgr;
    @Getter
    public static ApiCommandManager apiCommandManager;
    @Getter
    public static TelegramApi telegramApi;
    @Getter
    public static UserInfo userInfo;
    @Getter
    public static BuildInfo buildInfo;
    @Getter
    public static boolean Debug;


    public Extazyy() {
        userInfo = new UserInfo("Fruzz", 1, "Admin", 1720434096, true);
        buildInfo = new BuildInfo("Beta", "0.1", 1720434096);
        instance = this;
        moduleManager = new ModuleManager();
        font = new FontRenderers();
        font.init();
        themesUtil = new ThemesUtil();
        themesUtil.init();
        commandmgr = new CommandManager();
        commandmgr.init();
        friendmgr = new FriendManager();
        friendmgr.init();
        configmgr = new ConfigManager();
        configmgr.init();
        apiCommandManager = new ApiCommandManager();
        apiCommandManager.init();
        DragManager.init();
        Discord.startRPC();
        DrawShader.init();
        Gif.init();
        telegramApi = new TelegramApi("7713858680:AAEzKXxL8dETJFkiUx_Pm8du44TxEa501HI", "993032615");

        Runtime.getRuntime().addShutdownHook(new Thread(Extazyy::shutDown));
    }


    public static void shutDown() {
        DragManager.save();
        configmgr.saveConfiguration("autoconf");
        Discord.stopRPC();
    }

    public static void keyPress(int key) {
        if(mc.screen != null) return;

        if(key == GLFW.GLFW_KEY_BACKSLASH) {
            mc.setScreen(new ClickGui(Component.translatable("SPERMA")));
        }
        if(key == GLFW.GLFW_KEY_RIGHT_ALT) {
            mc.setScreen(new ThemeGui(Component.translatable("zalupa")));
        }
        if(key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            mc.setScreen(new UmbrellaGui());
        }

        for (Module m : moduleManager.getFunctions()) {
            if (m.bind == key) {
                m.toggle();
            }
        }
    }



    public static Dragging createDrag(Module module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }
}
