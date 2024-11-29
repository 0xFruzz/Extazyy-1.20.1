package net.minecraftforge.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@FunctionalInterface
public interface IWeatherParticleRenderHandler {
   void render(int var1, ClientLevel var2, Minecraft var3, Camera var4);
}