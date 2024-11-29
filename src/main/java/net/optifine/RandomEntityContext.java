package net.optifine;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.optifine.config.ConnectedParser;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.entity.model.IEntityRenderer;
import net.optifine.entity.model.RendererCache;

public interface RandomEntityContext<T> {
   String getName();

   String[] getResourceKeys();

   String getResourceName();

   T makeResource(ResourceLocation var1, int var2);

   default String getResourceNameCapital() {
      return this.getResourceName().substring(0, 1).toUpperCase() + this.getResourceName().substring(1);
   }

   default String getResourceNamePlural() {
      return this.getResourceName() + "s";
   }

   default ConnectedParser getConnectedParser() {
      return new ConnectedParser(this.getName());
   }

   public static class Models implements RandomEntityContext<IEntityRenderer> {
      private RendererCache rendererCache = new RendererCache();

      public String getName() {
         return "CustomEntityModels";
      }

      public String[] getResourceKeys() {
         return new String[]{"models"};
      }

      public String getResourceName() {
         return "model";
      }

      public IEntityRenderer makeResource(ResourceLocation locBase, int index) {
         ResourceLocation resourcelocation = index <= 1 ? locBase : RandomEntities.getLocationIndexed(locBase, index);
         if (resourcelocation == null) {
            Config.warn("Invalid path: " + locBase.getPath());
            return null;
         } else {
            IEntityRenderer ientityrenderer = CustomEntityModels.parseEntityRender(resourcelocation, this.rendererCache, index);
            if (ientityrenderer == null) {
               Config.warn("Model not found: " + resourcelocation.getPath());
               return null;
            } else {
               if (ientityrenderer instanceof EntityRenderer) {
                  this.rendererCache.put(ientityrenderer.getType().getLeft().get(), index, (EntityRenderer)ientityrenderer);
               } else if (ientityrenderer instanceof BlockEntityRenderer) {
                  this.rendererCache.put(ientityrenderer.getType().getRight().get(), index, (BlockEntityRenderer)ientityrenderer);
               }

               return ientityrenderer;
            }
         }
      }

      public RendererCache getRendererCache() {
         return this.rendererCache;
      }
   }

   public static class Textures implements RandomEntityContext<ResourceLocation> {
      private boolean legacy;

      public Textures(boolean legacy) {
         this.legacy = legacy;
      }

      public String getName() {
         return "RandomEntities";
      }

      public String[] getResourceKeys() {
         return new String[]{"textures", "skins"};
      }

      public String getResourceName() {
         return "texture";
      }

      public ResourceLocation makeResource(ResourceLocation locBase, int index) {
         if (index <= 1) {
            return locBase;
         } else {
            ResourceLocation resourcelocation = RandomEntities.getLocationRandom(locBase, this.legacy);
            if (resourcelocation == null) {
               Config.warn("Invalid path: " + locBase.getPath());
               return null;
            } else {
               ResourceLocation resourcelocation1 = RandomEntities.getLocationIndexed(resourcelocation, index);
               if (resourcelocation1 == null) {
                  Config.warn("Invalid path: " + locBase.getPath());
                  return null;
               } else if (!Config.hasResource(resourcelocation1)) {
                  Config.warn("Texture not found: " + resourcelocation1.getPath());
                  return null;
               } else {
                  return resourcelocation1;
               }
            }
         }
      }

      public boolean isLegacy() {
         return this.legacy;
      }
   }
}