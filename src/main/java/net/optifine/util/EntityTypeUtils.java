package net.optifine.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class EntityTypeUtils {
   public static EntityType getEntityType(ResourceLocation loc) {
      return !BuiltInRegistries.ENTITY_TYPE.containsKey(loc) ? null : BuiltInRegistries.ENTITY_TYPE.get(loc);
   }
}