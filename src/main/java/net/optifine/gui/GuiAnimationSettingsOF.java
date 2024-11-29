package net.optifine.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.optifine.Lang;
import net.optifine.config.Option;

public class GuiAnimationSettingsOF extends GuiScreenOF {
   private Screen prevScreen;
   private Options settings;

   public GuiAnimationSettingsOF(Screen guiscreen, Options gamesettings) {
      super(Component.translatable("of.options.animationsTitle"));
      this.prevScreen = guiscreen;
      this.settings = gamesettings;
   }

   public void init() {
      this.clearWidgets();
      OptionInstance[] aoptioninstance = new OptionInstance[]{Option.ANIMATED_WATER, Option.ANIMATED_LAVA, Option.ANIMATED_FIRE, Option.ANIMATED_PORTAL, Option.ANIMATED_REDSTONE, Option.ANIMATED_EXPLOSION, Option.ANIMATED_FLAME, Option.ANIMATED_SMOKE, Option.VOID_PARTICLES, Option.WATER_PARTICLES, Option.RAIN_SPLASH, Option.PORTAL_PARTICLES, Option.POTION_PARTICLES, Option.DRIPPING_WATER_LAVA, Option.ANIMATED_TERRAIN, Option.ANIMATED_TEXTURES, Option.FIREWORK_PARTICLES, this.settings.PARTICLES};

      for(int i = 0; i < aoptioninstance.length; ++i) {
         OptionInstance optioninstance = aoptioninstance[i];
         int j = this.width / 2 - 155 + i % 2 * 160;
         int k = this.height / 6 + 21 * (i / 2) - 12;
         AbstractWidget abstractwidget = this.addRenderableWidget(optioninstance.createButton(this.minecraft.options, j, k, 150));
         abstractwidget.setTooltip((Tooltip)null);
      }

      this.addRenderableWidget(new GuiButtonOF(210, this.width / 2 - 155, this.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOn")));
      this.addRenderableWidget(new GuiButtonOF(211, this.width / 2 - 155 + 80, this.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOff")));
      this.addRenderableWidget(new GuiScreenButtonOF(200, this.width / 2 + 5, this.height / 6 + 168 + 11, I18n.get("gui.done")));
   }

   protected void actionPerformed(AbstractWidget guiElement) {
      if (guiElement instanceof GuiButtonOF guibuttonof) {
         if (guibuttonof.active) {
            if (guibuttonof.id == 200) {
               this.minecraft.options.save();
               this.minecraft.setScreen(this.prevScreen);
            }

            if (guibuttonof.id == 210) {
               this.minecraft.options.setAllAnimations(true);
            }

            if (guibuttonof.id == 211) {
               this.minecraft.options.setAllAnimations(false);
            }

            this.minecraft.resizeDisplay();
         }
      }
   }

   public void removed() {
      this.minecraft.options.save();
      super.removed();
   }

   public void render(GuiGraphics graphicsIn, int x, int y, float partialTicks) {
      this.renderBackground(graphicsIn);
      drawCenteredString(graphicsIn, this.minecraft.font, this.title, this.width / 2, 15, 16777215);
      super.render(graphicsIn, x, y, partialTicks);
   }
}