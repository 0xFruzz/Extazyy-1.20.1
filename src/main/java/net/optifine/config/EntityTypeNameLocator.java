package net.optifine.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.optifine.util.EntityTypeUtils;

public class EntityTypeNameLocator implements IObjectLocator<String> {
   public String getObject(ResourceLocation loc) {
      EntityType entitytype = EntityTypeUtils.getEntityType(loc);
      return entitytype == null ? null : entitytype.getDescriptionId();
   }

   public static String getEntityTypeName(Entity entity) {
      return entity.getType().getDescriptionId();
   }
}