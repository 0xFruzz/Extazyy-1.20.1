package net.optifine.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.optifine.Config;
import net.optifine.Lang;

public class FloatOptions {
   public static Component getTextComponent(OptionInstance option, double val) {
      Options options = Minecraft.getInstance().options;
      String s = I18n.get(option.getResourceKey()) + ": ";
      if (option == options.RENDER_DISTANCE) {
         return Options.genericValueLabel(option.getResourceKey(), "options.chunks", (int)val);
      } else if (option == options.MIPMAP_LEVELS) {
         if (val >= 4.0D) {
            return Options.genericValueLabel(option.getResourceKey(), "of.general.max");
         } else {
            return (Component)(val == 0.0D ? CommonComponents.optionStatus(option.getCaption(), false) : Options.genericValueLabel(option.getResourceKey(), (int)val));
         }
      } else if (option == options.BIOME_BLEND_RADIUS) {
         int i = (int)val * 2 + 1;
         return Options.genericValueLabel(option.getResourceKey(), "options.biomeBlendRadius." + i);
      } else {
         String s1 = getText(option, val);
         return s1 != null ? Component.literal(s1) : null;
      }
   }

   public static String getText(OptionInstance option, double val) {
      String s = I18n.get(option.getResourceKey()) + ": ";
      if (option == Option.AO_LEVEL) {
         return val == 0.0D ? s + I18n.get("options.off") : s + (int)(val * 100.0D) + "%";
      } else if (option == Option.MIPMAP_TYPE) {
         int k = (int)val;
         switch (k) {
            case 0:
               return s + Lang.get("of.options.mipmap.nearest");
            case 1:
               return s + Lang.get("of.options.mipmap.linear");
            case 2:
               return s + Lang.get("of.options.mipmap.bilinear");
            case 3:
               return s + Lang.get("of.options.mipmap.trilinear");
            default:
               return s + "of.options.mipmap.nearest";
         }
      } else if (option == Option.AA_LEVEL) {
         int j = (int)val;
         String s1 = "";
         if (j != Config.getAntialiasingLevel()) {
            s1 = " (" + Lang.get("of.general.restart") + ")";
         }

         return j == 0 ? s + Lang.getOff() + s1 : s + j + s1;
      } else if (option == Option.AF_LEVEL) {
         int i = (int)val;
         return i == 1 ? s + Lang.getOff() : s + i;
      } else {
         return null;
      }
   }

   public static boolean supportAdjusting(OptionInstance option) {
      Component component = getTextComponent(option, 0.0D);
      return component != null;
   }
}