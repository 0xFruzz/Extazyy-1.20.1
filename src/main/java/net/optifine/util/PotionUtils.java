package net.optifine.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class PotionUtils {
   public static MobEffect getPotion(ResourceLocation loc) {
      return !BuiltInRegistries.MOB_EFFECT.containsKey(loc) ? null : BuiltInRegistries.MOB_EFFECT.get(loc);
   }
}