package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.optifine.Config;
import net.optifine.shaders.Shaders;

public abstract class EyesLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   public EyesLayer(RenderLayerParent<T, M> pRenderer) {
      super(pRenderer);
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      VertexConsumer vertexconsumer = pBuffer.getBuffer(this.renderType());
      if (Config.isShaders()) {
         Shaders.beginSpiderEyes();
      }

      Config.getRenderGlobal().renderOverlayEyes = true;
      this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      Config.getRenderGlobal().renderOverlayEyes = false;
      if (Config.isShaders()) {
         Shaders.endSpiderEyes();
      }

   }

   public abstract RenderType renderType();
}