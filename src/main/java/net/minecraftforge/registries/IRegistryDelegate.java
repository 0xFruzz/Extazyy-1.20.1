package net.minecraftforge.registries;

import net.minecraft.resources.ResourceLocation;

public interface IRegistryDelegate<T> {
   T get();

   ResourceLocation name();

   Class<T> type();
}