package net.optifine.entity.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.optifine.Config;

public class ModelAdapterRaft extends ModelAdapter {
   public ModelAdapterRaft() {
      super(EntityType.BOAT, "raft", 0.5F);
   }

   protected ModelAdapterRaft(EntityType entityType, String name, float shadowSize) {
      super(entityType, name, shadowSize);
   }

   public Model makeModel() {
      return new RaftModel(bakeModelLayer(ModelLayers.createBoatModelName(Boat.Type.BAMBOO)));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof RaftModel raftmodel)) {
         return null;
      } else {
         ImmutableList<ModelPart> immutablelist = raftmodel.parts();
         if (immutablelist != null) {
            if (modelPart.equals("bottom")) {
               return ModelRendererUtils.getModelRenderer(immutablelist, 0);
            }

            if (modelPart.equals("paddle_left")) {
               return ModelRendererUtils.getModelRenderer(immutablelist, 1);
            }

            if (modelPart.equals("paddle_right")) {
               return ModelRendererUtils.getModelRenderer(immutablelist, 2);
            }
         }

         return null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"bottom", "paddle_left", "paddle_right"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      BoatRenderer boatrenderer = new BoatRenderer(entityrenderdispatcher.getContext(), false);
      EntityRenderer entityrenderer = rendererCache.get(EntityType.BOAT, index, () -> {
         return boatrenderer;
      });
      if (!(entityrenderer instanceof BoatRenderer boatrenderer1)) {
         Config.warn("Not a BoatRender: " + entityrenderer);
         return null;
      } else {
         return ModelAdapterBoat.makeEntityRender(modelBase, shadowSize, boatrenderer1);
      }
   }
}