package net.optifine.gui;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public class TooltipManager {
   private Screen guiScreen;
   private TooltipProvider tooltipProvider;
   private int lastMouseX = 0;
   private int lastMouseY = 0;
   private long mouseStillTime = 0L;

   public TooltipManager(Screen guiScreen, TooltipProvider tooltipProvider) {
      this.guiScreen = guiScreen;
      this.tooltipProvider = tooltipProvider;
   }

   public void drawTooltips(GuiGraphics graphicsIn, int x, int y, List<AbstractWidget> buttonList) {
      if (Math.abs(x - this.lastMouseX) <= 5 && Math.abs(y - this.lastMouseY) <= 5) {
         int i = 700;
         if (System.currentTimeMillis() >= this.mouseStillTime + (long)i) {
            AbstractWidget abstractwidget = GuiScreenOF.getSelectedButton(x, y, buttonList);
            if (abstractwidget != null) {
               Rectangle rectangle = this.tooltipProvider.getTooltipBounds(this.guiScreen, x, y);
               String[] astring = this.tooltipProvider.getTooltipLines(abstractwidget, rectangle.width);
               if (astring != null) {
                  if (astring.length > 8) {
                     astring = Arrays.copyOf(astring, 8);
                     astring[astring.length - 1] = astring[astring.length - 1] + " ...";
                  }

                  graphicsIn.pose().pushPose();
                  graphicsIn.pose().translate(0.0F, 0.0F, 400.0F);
                  if (this.tooltipProvider.isRenderBorder()) {
                     int j = -528449408;
                     this.drawRectBorder(graphicsIn, rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, j);
                  }

                  graphicsIn.fill(rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, -536870912);

                  for(int l = 0; l < astring.length; ++l) {
                     String s = astring[l];
                     int k = 14540253;
                     if (s.endsWith("!")) {
                        k = 16719904;
                     }

                     Font font = Minecraft.getInstance().font;
                     graphicsIn.drawString(font, s, rectangle.x + 5, rectangle.y + 5 + l * 11, k, true);
                  }

                  graphicsIn.pose().popPose();
               }
            }
         }
      } else {
         this.lastMouseX = x;
         this.lastMouseY = y;
         this.mouseStillTime = System.currentTimeMillis();
      }
   }

   private void drawRectBorder(GuiGraphics graphicsIn, int x1, int y1, int x2, int y2, int col) {
      graphicsIn.fill(x1, y1 - 1, x2, y1, col);
      graphicsIn.fill(x1, y2, x2, y2 + 1, col);
      graphicsIn.fill(x1 - 1, y1, x1, y2, col);
      graphicsIn.fill(x2, y1, x2 + 1, y2, col);
   }
}