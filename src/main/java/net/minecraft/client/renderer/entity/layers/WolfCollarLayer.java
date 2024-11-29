package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.entity.model.ModelAdapter;

public class WolfCollarLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
   private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_collar.png");
   public WolfModel<Wolf> model = new WolfModel<>(ModelAdapter.bakeModelLayer(ModelLayers.WOLF));

   public WolfCollarLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> pRenderer) {
      super(pRenderer);
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Wolf pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (pLivingEntity.isTame() && !pLivingEntity.isInvisible()) {
         float[] afloat = pLivingEntity.getCollarColor().getTextureDiffuseColors();
         if (Config.isCustomColors()) {
            afloat = CustomColors.getWolfCollarColors(pLivingEntity.getCollarColor(), afloat);
         }

         coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, WOLF_COLLAR_LOCATION, pPoseStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, afloat[0], afloat[1], afloat[2]);
      }

   }
}