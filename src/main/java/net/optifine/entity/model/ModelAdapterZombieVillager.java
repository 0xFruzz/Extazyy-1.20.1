package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ZombieVillagerRenderer;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterZombieVillager extends ModelAdapterBiped {
   public ModelAdapterZombieVillager() {
      super(EntityType.ZOMBIE_VILLAGER, "zombie_villager", 0.5F);
   }

   public Model makeModel() {
      return new ZombieVillagerModel(bakeModelLayer(ModelLayers.ZOMBIE_VILLAGER));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      ReloadableResourceManager reloadableresourcemanager = (ReloadableResourceManager)Minecraft.getInstance().getResourceManager();
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      ZombieVillagerRenderer zombievillagerrenderer = new ZombieVillagerRenderer(entityrenderdispatcher.getContext());
      zombievillagerrenderer.model = (ZombieVillagerModel)modelBase;
      zombievillagerrenderer.shadowRadius = shadowSize;
      return zombievillagerrenderer;
   }
}