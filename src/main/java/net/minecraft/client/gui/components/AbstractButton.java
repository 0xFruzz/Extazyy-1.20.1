package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.misc.util.color.ColorUtils;
import ru.fruzz.extazyy.misc.util.render.lowrender.RenderMcd;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractButton extends AbstractWidget {
   protected static final int TEXTURE_Y_OFFSET = 46;
   protected static final int TEXTURE_WIDTH = 200;
   protected static final int TEXTURE_HEIGHT = 20;
   protected static final int TEXTURE_BORDER_X = 20;
   protected static final int TEXTURE_BORDER_Y = 4;
   protected static final int TEXT_MARGIN = 2;

   public AbstractButton(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
      super(pX, pY, pWidth, pHeight, pMessage);
   }

   public abstract void onPress();

   protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      RenderMcd.drawBlurredShadow(pGuiGraphics.pose(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), 8, isHovered ? ColorUtils.getColorStyleLowSpeed(90) : new Color(20, 20, 20, 0).getRGB());
      DrawHelper.rectangle(pGuiGraphics.pose(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), 2, new Color(20, 20, 20, 217).getRGB());
      FontRenderers.msSemi16.drawCenteredString(pGuiGraphics.pose(), this.getMessage().getString() , this.getX() + this.getWidth() / 2 , this.getY() + (this.getHeight() / 2) - 3, new Color(255,255,255,255).getRGB());
     }

   public void renderString(GuiGraphics pGuiGraphics, Font pFont, int pColor) {
      this.renderScrollingString(pGuiGraphics, pFont, 2, pColor);
   }

   private int getTextureY() {
      int i = 1;
      if (!this.active) {
         i = 0;
      } else if (this.isHoveredOrFocused()) {
         i = 2;
      }

      return 46 + i * 20;
   }

   public void onClick(double pMouseX, double pMouseY) {
      this.onPress();
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.active && this.visible) {
         if (CommonInputs.selected(pKeyCode)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}