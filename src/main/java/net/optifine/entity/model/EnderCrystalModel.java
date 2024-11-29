package net.optifine.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class EnderCrystalModel extends Model {
   public ModelPart cube;
   public ModelPart glass;
   public ModelPart base;

   public EnderCrystalModel() {
      super(RenderType::entityCutoutNoCull);
      EndCrystalRenderer endcrystalrenderer = new EndCrystalRenderer(Minecraft.getInstance().getEntityRenderDispatcher().getContext());
      this.cube = (ModelPart)Reflector.RenderEnderCrystal_modelRenderers.getValue(endcrystalrenderer, 0);
      this.glass = (ModelPart)Reflector.RenderEnderCrystal_modelRenderers.getValue(endcrystalrenderer, 1);
      this.base = (ModelPart)Reflector.RenderEnderCrystal_modelRenderers.getValue(endcrystalrenderer, 2);
   }

   public EndCrystalRenderer updateRenderer(EndCrystalRenderer render) {
      if (!Reflector.RenderEnderCrystal_modelRenderers.exists()) {
         Config.warn("Field not found: RenderEnderCrystal.modelEnderCrystal");
         return null;
      } else {
         Reflector.RenderEnderCrystal_modelRenderers.setValue(render, 0, this.cube);
         Reflector.RenderEnderCrystal_modelRenderers.setValue(render, 1, this.glass);
         Reflector.RenderEnderCrystal_modelRenderers.setValue(render, 2, this.base);
         return render;
      }
   }

   public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
   }
}