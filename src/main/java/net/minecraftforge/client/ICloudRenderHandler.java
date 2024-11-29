package net.minecraftforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@FunctionalInterface
public interface ICloudRenderHandler {
   void render(int var1, float var2, PoseStack var3, ClientLevel var4, Minecraft var5, double var6, double var8, double var10);
}