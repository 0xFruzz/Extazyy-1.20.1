package net.optifine.config;

import net.minecraft.resources.ResourceLocation;

public interface IObjectLocator<T> {
   T getObject(ResourceLocation var1);
}