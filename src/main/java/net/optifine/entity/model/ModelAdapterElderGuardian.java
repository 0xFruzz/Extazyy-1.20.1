package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterElderGuardian extends ModelAdapterGuardian {
   public ModelAdapterElderGuardian() {
      super(EntityType.ELDER_GUARDIAN, "elder_guardian", 0.5F);
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      ElderGuardianRenderer elderguardianrenderer = new ElderGuardianRenderer(entityrenderdispatcher.getContext());
      elderguardianrenderer.model = (GuardianModel)modelBase;
      elderguardianrenderer.shadowRadius = shadowSize;
      return elderguardianrenderer;
   }
}