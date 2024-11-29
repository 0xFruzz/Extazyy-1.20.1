package net.minecraft.client.resources.model;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.IForgeModelBaker;

public interface ModelBaker extends IForgeModelBaker {
   UnbakedModel getModel(ResourceLocation pLocation);

   @Nullable
   BakedModel bake(ResourceLocation pLocation, ModelState pTransform);
}