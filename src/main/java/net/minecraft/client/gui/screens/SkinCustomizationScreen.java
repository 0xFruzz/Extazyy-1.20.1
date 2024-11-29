package net.minecraft.client.gui.screens;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.optifine.Lang;
import net.optifine.gui.GuiScreenCapeOF;

public class SkinCustomizationScreen extends OptionsSubScreen {
   public SkinCustomizationScreen(Screen pLastScreen, Options pOptions) {
      super(pLastScreen, pOptions, Component.translatable("options.skinCustomisation.title"));
   }

   protected void init() {
      int i = 0;

      for(PlayerModelPart playermodelpart : PlayerModelPart.values()) {
         this.addRenderableWidget(CycleButton.onOffBuilder(this.options.isModelPartEnabled(playermodelpart)).create(this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 150, 20, playermodelpart.getName(), (p_169434_2_, p_169434_3_) -> {
            this.options.toggleModelPart(playermodelpart, p_169434_3_);
         }));
         ++i;
      }

      this.addRenderableWidget(this.options.mainHand().createButton(this.options, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 150));
      ++i;
      if (i % 2 == 1) {
         ++i;
      }

      this.addRenderableWidget(Button.builder(Lang.getComponent("of.options.skinCustomisation.ofCape"), (button) -> {
         this.minecraft.setScreen(new GuiScreenCapeOF(this));
      }).pos(this.width / 2 - 100, this.height / 6 + 24 * (i >> 1)).size(200, 20).build());
      i += 2;
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_279791_1_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), 200, 20).build());
   }

   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}