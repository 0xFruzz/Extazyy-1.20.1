package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterLlama extends ModelAdapter {
   public ModelAdapterLlama() {
      super(EntityType.LLAMA, "llama", 0.7F);
   }

   public ModelAdapterLlama(EntityType entityType, String name, float shadowSize) {
      super(entityType, name, shadowSize);
   }

   public Model makeModel() {
      return new LlamaModel(bakeModelLayer(ModelLayers.LLAMA));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof LlamaModel llamamodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return (ModelPart)Reflector.ModelLlama_ModelRenderers.getValue(llamamodel, 0);
      } else if (modelPart.equals("body")) {
         return (ModelPart)Reflector.ModelLlama_ModelRenderers.getValue(llamamodel, 1);
      } else if (modelPart.equals("leg1")) {
         return (ModelPart)Reflector.ModelLlama_ModelRenderers.getValue(llamamodel, 2);
      } else if (modelPart.equals("leg2")) {
         return (ModelPart)Reflector.ModelLlama_ModelRenderers.getValue(llamamodel, 3);
      } else if (modelPart.equals("leg3")) {
         return (ModelPart)Reflector.ModelLlama_ModelRenderers.getValue(llamamodel, 4);
      } else if (modelPart.equals("leg4")) {
         return (ModelPart)Reflector.ModelLlama_ModelRenderers.getValue(llamamodel, 5);
      } else if (modelPart.equals("chest_right")) {
         return (ModelPart)Reflector.ModelLlama_ModelRenderers.getValue(llamamodel, 6);
      } else {
         return modelPart.equals("chest_left") ? (ModelPart)Reflector.ModelLlama_ModelRenderers.getValue(llamamodel, 7) : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "body", "leg1", "leg2", "leg3", "leg4", "chest_right", "chest_left"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      LlamaRenderer llamarenderer = new LlamaRenderer(entityrenderdispatcher.getContext(), ModelLayers.LLAMA);
      llamarenderer.model = (LlamaModel)modelBase;
      llamarenderer.shadowRadius = shadowSize;
      return llamarenderer;
   }
}