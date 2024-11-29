package net.optifine.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.optifine.Config;

public class PlayerItemsLayer extends RenderLayer {
   private PlayerRenderer renderPlayer = null;

   public PlayerItemsLayer(PlayerRenderer renderPlayer) {
      super(renderPlayer);
      this.renderPlayer = renderPlayer;
   }

   public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Entity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
      this.renderEquippedItems(entitylivingbaseIn, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY);
   }

   protected void renderEquippedItems(Entity entityLiving, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, int packedOverlayIn) {
      if (Config.isShowCapes()) {
         if (!entityLiving.isInvisible()) {
            if (entityLiving instanceof AbstractClientPlayer) {
               AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)entityLiving;
               HumanoidModel humanoidmodel = this.renderPlayer.getModel();
               PlayerConfigurations.renderPlayerItems(humanoidmodel, abstractclientplayer, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            }
         }
      }
   }
}