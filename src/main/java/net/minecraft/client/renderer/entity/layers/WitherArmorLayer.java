package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public class WitherArmorLayer extends EnergySwirlLayer<WitherBoss, WitherBossModel<WitherBoss>> {
   private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
   public WitherBossModel<WitherBoss> model;
   public ResourceLocation customTextureLocation;

   public WitherArmorLayer(RenderLayerParent<WitherBoss, WitherBossModel<WitherBoss>> pRenderer, EntityModelSet pModelSet) {
      super(pRenderer);
      this.model = new WitherBossModel<>(pModelSet.bakeLayer(ModelLayers.WITHER_ARMOR));
   }

   protected float xOffset(float pTickCount) {
      return Mth.cos(pTickCount * 0.02F) * 3.0F;
   }

   protected ResourceLocation getTextureLocation() {
      return this.customTextureLocation != null ? this.customTextureLocation : WITHER_ARMOR_LOCATION;
   }

   protected EntityModel<WitherBoss> model() {
      return this.model;
   }
}