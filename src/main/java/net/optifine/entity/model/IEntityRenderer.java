package net.optifine.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.util.Either;

public interface IEntityRenderer {
   Either<EntityType, BlockEntityType> getType();

   void setType(Either<EntityType, BlockEntityType> var1);

   ResourceLocation getLocationTextureCustom();

   void setLocationTextureCustom(ResourceLocation var1);
}