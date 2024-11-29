package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterArmorStand extends ModelAdapterBiped {
   public ModelAdapterArmorStand() {
      super(EntityType.ARMOR_STAND, "armor_stand", 0.0F);
   }

   public Model makeModel() {
      return new ArmorStandModel(bakeModelLayer(ModelLayers.ARMOR_STAND));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof ArmorStandModel armorstandmodel)) {
         return null;
      } else if (modelPart.equals("right")) {
         return (ModelPart)Reflector.getFieldValue(armorstandmodel, Reflector.ModelArmorStand_ModelRenderers, 0);
      } else if (modelPart.equals("left")) {
         return (ModelPart)Reflector.getFieldValue(armorstandmodel, Reflector.ModelArmorStand_ModelRenderers, 1);
      } else if (modelPart.equals("waist")) {
         return (ModelPart)Reflector.getFieldValue(armorstandmodel, Reflector.ModelArmorStand_ModelRenderers, 2);
      } else {
         return modelPart.equals("base") ? (ModelPart)Reflector.getFieldValue(armorstandmodel, Reflector.ModelArmorStand_ModelRenderers, 3) : super.getModelRenderer(armorstandmodel, modelPart);
      }
   }

   public String[] getModelRendererNames() {
      String[] astring = super.getModelRendererNames();
      return (String[])Config.addObjectsToArray(astring, new String[]{"right", "left", "waist", "base"});
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      ArmorStandRenderer armorstandrenderer = new ArmorStandRenderer(entityrenderdispatcher.getContext());
      armorstandrenderer.model = (ArmorStandArmorModel)modelBase;
      armorstandrenderer.shadowRadius = shadowSize;
      return armorstandrenderer;
   }
}