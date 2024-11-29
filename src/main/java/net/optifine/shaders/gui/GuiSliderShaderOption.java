package net.optifine.shaders.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.optifine.shaders.config.ShaderOption;

public class GuiSliderShaderOption extends GuiButtonShaderOption {
   private float sliderValue;
   public boolean dragging;
   private ShaderOption shaderOption = null;

   public GuiSliderShaderOption(int buttonId, int x, int y, int w, int h, ShaderOption shaderOption, String text) {
      super(buttonId, x, y, w, h, shaderOption, text);
      this.sliderValue = 1.0F;
      this.shaderOption = shaderOption;
      this.sliderValue = shaderOption.getIndexNormalized();
      this.setMessage(GuiShaderOptions.getButtonText(shaderOption, this.width));
   }

   public void renderWidget(GuiGraphics graphicsIn, int mouseX, int mouseY, float partialTicks) {
      if (this.visible) {
         if (this.dragging && !Screen.hasShiftDown()) {
            this.sliderValue = (float)(mouseX - (this.getX() + 4)) / (float)(this.width - 8);
            this.sliderValue = Mth.clamp(this.sliderValue, 0.0F, 1.0F);
            this.shaderOption.setIndexNormalized(this.sliderValue);
            this.sliderValue = this.shaderOption.getIndexNormalized();
            this.setMessage(GuiShaderOptions.getButtonText(this.shaderOption, this.width));
         }

         Minecraft minecraft = Minecraft.getInstance();
         RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         int i = (this.isHovered() ? 2 : 1) * 20;
         this.blitNineSliced(graphicsIn, WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, 46);
         blit(graphicsIn, WIDGETS_LOCATION, this.getX() + (int)(this.sliderValue * (float)(this.width - 8)), this.getY(), 0, 46 + i, 4, 20);
         blit(graphicsIn, WIDGETS_LOCATION, this.getX() + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.getY(), 196, 46 + i, 4, 20);
         int j = this.active ? 16777215 : 10526880;
         this.renderString(graphicsIn, minecraft.font, j | Mth.ceil(this.alpha * 255.0F) << 24);
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         this.sliderValue = (float)(mouseX - (double)(this.getX() + 4)) / (float)(this.width - 8);
         this.sliderValue = Mth.clamp(this.sliderValue, 0.0F, 1.0F);
         this.shaderOption.setIndexNormalized(this.sliderValue);
         this.setMessage(GuiShaderOptions.getButtonText(this.shaderOption, this.width));
         this.dragging = true;
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.dragging = false;
      return true;
   }

   public void valueChanged() {
      this.sliderValue = this.shaderOption.getIndexNormalized();
   }

   public boolean isSwitchable() {
      return false;
   }
}