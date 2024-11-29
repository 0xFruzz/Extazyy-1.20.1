package net.optifine.gui;

import java.awt.Rectangle;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public interface TooltipProvider {
   Rectangle getTooltipBounds(Screen var1, int var2, int var3);

   String[] getTooltipLines(AbstractWidget var1, int var2);

   boolean isRenderBorder();
}