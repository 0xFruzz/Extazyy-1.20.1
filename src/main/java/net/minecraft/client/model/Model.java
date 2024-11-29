package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.optifine.EmissiveTextures;

public abstract class Model {
   protected final Function<ResourceLocation, RenderType> renderType;
   public int textureWidth = 64;
   public int textureHeight = 32;
   public ResourceLocation locationTextureCustom;

   public Model(Function<ResourceLocation, RenderType> pRenderType) {
      this.renderType = pRenderType;
   }

   public final RenderType renderType(ResourceLocation pLocation) {
      RenderType rendertype = this.renderType.apply(pLocation);
      if (EmissiveTextures.isRenderEmissive() && rendertype.isEntitySolid()) {
         rendertype = RenderType.entityCutout(pLocation);
      }

      return rendertype;
   }

   public abstract void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha);
}