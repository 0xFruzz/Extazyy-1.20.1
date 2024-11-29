package ru.fruzz.extazyy.misc.util;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;
import ru.fruzz.extazyy.misc.util.color.ColorUtils;

import java.awt.*;

public class ClientUtil implements Mine {

    public static boolean legitMode = false;

    public static void sendMessage(String message) {
        if (mc.player == null) return;
        mc.player.sendSystemMessage(gradient("Extazyy | ", ColorUtils.getColorStyle(0),ColorUtils.getColorStyle(255)).append(message));

    }

   public static MutableComponent gradient(String message, int first, int end) {
        MutableComponent text = Component.literal("");
        for (int i = 0; i < message.length(); i++) {
            text.append(Component.translatable(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.withColor(new Color(ColorUtils.interpolateColor(first, end, (float) i / message.length())).getRGB())));
        }
        return text;
    }

    public static String getKey(int integer) {
        if (integer < 0) {
            return switch (integer) {
                case -100 -> I18n.get("key.mouse.left");
                case -99 -> I18n.get("key.mouse.right");
                case -98 -> I18n.get("key.mouse.middle");
                default -> "M" + (integer + 101);
            };
        } else {
            return (GLFW.glfwGetKeyName(integer, -1) == null ? KeyMappings.reverseKeyMap.get(integer) : GLFW.glfwGetKeyName(integer, -1)) ;
        }
    }



}
