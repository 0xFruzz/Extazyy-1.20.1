package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Renderable {
   void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick);
}