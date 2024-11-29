package net.minecraftforge.fml.common.registry;

import net.minecraft.resources.ResourceLocation;

public interface RegistryDelegate<T> {
   T get();

   ResourceLocation name();

   Class<T> type();
}