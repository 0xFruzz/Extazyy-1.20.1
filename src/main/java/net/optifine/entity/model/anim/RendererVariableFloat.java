package net.optifine.entity.model.anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;

public enum RendererVariableFloat implements IModelVariableFloat {
   SHADOW_SIZE("shadow_size"),
   SHADOW_OPACITY("shadow_opacity"),
   LEASH_OFFSET_X("leash_offset_x"),
   LEASH_OFFSET_Y("leash_offset_y"),
   LEASH_OFFSET_Z("leash_offset_z"),
   SHADOW_OFFSET_X("shadow_offset_x"),
   SHADOW_OFFSET_Z("shadow_offset_z");

   private String name;
   private EntityRenderDispatcher renderManager;
   private static final RendererVariableFloat[] VALUES = values();

   private RendererVariableFloat(String name) {
      this.name = name;
      this.renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
   }

   public float eval() {
      return this.getValue();
   }

   public float getValue() {
      EntityRenderer entityrenderer = this.renderManager.getEntityRenderer();
      if (entityrenderer == null) {
         return 0.0F;
      } else {
         switch (this) {
            case SHADOW_SIZE:
               return entityrenderer.shadowRadius;
            case SHADOW_OPACITY:
               return entityrenderer.shadowStrength;
            case SHADOW_OFFSET_X:
               return entityrenderer.shadowOffsetX;
            case SHADOW_OFFSET_Z:
               return entityrenderer.shadowOffsetZ;
            default:
               if (entityrenderer instanceof MobRenderer) {
                  MobRenderer mobrenderer = (MobRenderer)entityrenderer;
                  switch (this) {
                     case LEASH_OFFSET_X:
                        return mobrenderer.leashOffsetX;
                     case LEASH_OFFSET_Y:
                        return mobrenderer.leashOffsetY;
                     case LEASH_OFFSET_Z:
                        return mobrenderer.leashOffsetZ;
                  }
               }

               return 0.0F;
         }
      }
   }

   public void setValue(float value) {
      EntityRenderer entityrenderer = this.renderManager.getEntityRenderer();
      if (entityrenderer != null) {
         switch (this) {
            case SHADOW_SIZE:
               entityrenderer.shadowRadius = value;
               return;
            case SHADOW_OPACITY:
               entityrenderer.shadowStrength = value;
               return;
            case SHADOW_OFFSET_X:
               entityrenderer.shadowOffsetX = value;
               return;
            case SHADOW_OFFSET_Z:
               entityrenderer.shadowOffsetZ = value;
               return;
            default:
               if (entityrenderer instanceof MobRenderer) {
                  MobRenderer mobrenderer = (MobRenderer)entityrenderer;
                  switch (this) {
                     case LEASH_OFFSET_X:
                        mobrenderer.leashOffsetX = value;
                        return;
                     case LEASH_OFFSET_Y:
                        mobrenderer.leashOffsetY = value;
                        return;
                     case LEASH_OFFSET_Z:
                        mobrenderer.leashOffsetZ = value;
                        return;
                  }
               }

         }
      }
   }

   public String getName() {
      return this.name;
   }

   public static RendererVariableFloat parse(String str) {
      if (str == null) {
         return null;
      } else {
         for(int i = 0; i < VALUES.length; ++i) {
            RendererVariableFloat renderervariablefloat = VALUES[i];
            if (renderervariablefloat.getName().equals(str)) {
               return renderervariablefloat;
            }
         }

         return null;
      }
   }

   public String toString() {
      return this.name;
   }
}