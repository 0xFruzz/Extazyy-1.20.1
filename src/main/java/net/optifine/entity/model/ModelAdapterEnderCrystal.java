package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterEnderCrystal extends ModelAdapter {
   public ModelAdapterEnderCrystal() {
      this("end_crystal");
   }

   protected ModelAdapterEnderCrystal(String name) {
      super(EntityType.END_CRYSTAL, name, 0.5F);
   }

   public Model makeModel() {
      return new EnderCrystalModel();
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof EnderCrystalModel endercrystalmodel)) {
         return null;
      } else if (modelPart.equals("cube")) {
         return endercrystalmodel.cube;
      } else if (modelPart.equals("glass")) {
         return endercrystalmodel.glass;
      } else {
         return modelPart.equals("base") ? endercrystalmodel.base : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"cube", "glass", "base"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      EntityRenderer entityrenderer = rendererCache.get(EntityType.END_CRYSTAL, index, () -> {
         return new EndCrystalRenderer(entityrenderdispatcher.getContext());
      });
      if (!(entityrenderer instanceof EndCrystalRenderer endcrystalrenderer)) {
         Config.warn("Not an instance of RenderEnderCrystal: " + entityrenderer);
         return null;
      } else if (!(modelBase instanceof EnderCrystalModel endercrystalmodel)) {
         Config.warn("Not a EnderCrystalModel model: " + modelBase);
         return null;
      } else {
         endcrystalrenderer = endercrystalmodel.updateRenderer(endcrystalrenderer);
         endcrystalrenderer.shadowRadius = shadowSize;
         return endcrystalrenderer;
      }
   }
}