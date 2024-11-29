package net.optifine.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.optifine.config.Option;

public class GuiDetailSettingsOF extends GuiScreenOF {
   private Screen prevScreen;
   private Options settings;
   private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

   public GuiDetailSettingsOF(Screen guiscreen, Options gamesettings) {
      super(Component.literal(I18n.get("of.options.detailsTitle")));
      this.prevScreen = guiscreen;
      this.settings = gamesettings;
   }

   public void init() {
      this.clearWidgets();
      OptionInstance[] aoptioninstance = new OptionInstance[]{Option.CLOUDS, Option.CLOUD_HEIGHT, Option.TREES, Option.RAIN, Option.SKY, Option.STARS, Option.SUN_MOON, Option.SHOW_CAPES, Option.FOG_FANCY, Option.FOG_START, this.settings.VIEW_BOBBING, Option.HELD_ITEM_TOOLTIPS, this.settings.AUTOSAVE_INDICATOR, Option.SWAMP_COLORS, Option.VIGNETTE, Option.ALTERNATE_BLOCKS, this.settings.ENTITY_DISTANCE_SCALING, this.settings.BIOME_BLEND_RADIUS};

      for(int i = 0; i < aoptioninstance.length; ++i) {
         OptionInstance optioninstance = aoptioninstance[i];
         int j = this.width / 2 - 155 + i % 2 * 160;
         int k = this.height / 6 + 21 * (i / 2) - 12;
         AbstractWidget abstractwidget = this.addRenderableWidget(optioninstance.createButton(this.minecraft.options, j, k, 150));
         abstractwidget.setTooltip((Tooltip)null);
      }

      this.addRenderableWidget(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.get("gui.done")));
   }

   protected void actionPerformed(AbstractWidget guiElement) {
      if (guiElement instanceof GuiButtonOF guibuttonof) {
         if (guibuttonof.active) {
            if (guibuttonof.id == 200) {
               this.minecraft.options.save();
               this.minecraft.setScreen(this.prevScreen);
            }

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
      this.tooltipManager.drawTooltips(graphicsIn, x, y, this.getButtonList());
   }
}