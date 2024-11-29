package net.optifine.shaders.uniform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.optifine.expr.IExpressionBool;

public enum ShaderParameterBool implements IExpressionBool {
   IS_ALIVE("is_alive"),
   IS_BURNING("is_burning"),
   IS_CHILD("is_child"),
   IS_GLOWING("is_glowing"),
   IS_HURT("is_hurt"),
   IS_IN_LAVA("is_in_lava"),
   IS_IN_WATER("is_in_water"),
   IS_INVISIBLE("is_invisible"),
   IS_ON_GROUND("is_on_ground"),
   IS_RIDDEN("is_ridden"),
   IS_RIDING("is_riding"),
   IS_SNEAKING("is_sneaking"),
   IS_SPRINTING("is_sprinting"),
   IS_WET("is_wet");

   private String name;
   private EntityRenderDispatcher renderManager;
   private static final ShaderParameterBool[] VALUES = values();

   private ShaderParameterBool(String name) {
      this.name = name;
      this.renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
   }

   public String getName() {
      return this.name;
   }

   public boolean eval() {
      Entity entity = Minecraft.getInstance().getCameraEntity();
      if (entity instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)entity;
         switch (this) {
            case IS_ALIVE:
               return livingentity.isAlive();
            case IS_BURNING:
               return livingentity.isOnFire();
            case IS_CHILD:
               return livingentity.isBaby();
            case IS_GLOWING:
               return livingentity.isCurrentlyGlowing();
            case IS_HURT:
               return livingentity.hurtTime > 0;
            case IS_IN_LAVA:
               return livingentity.isInLava();
            case IS_IN_WATER:
               return livingentity.isInWater();
            case IS_INVISIBLE:
               return livingentity.isInvisible();
            case IS_ON_GROUND:
               return livingentity.onGround();
            case IS_RIDDEN:
               return livingentity.isVehicle();
            case IS_RIDING:
               return livingentity.isPassenger();
            case IS_SNEAKING:
               return livingentity.isCrouching();
            case IS_SPRINTING:
               return livingentity.isSprinting();
            case IS_WET:
               return livingentity.isInWaterOrRain();
         }
      }

      return false;
   }

   public static ShaderParameterBool parse(String str) {
      if (str == null) {
         return null;
      } else {
         for(int i = 0; i < VALUES.length; ++i) {
            ShaderParameterBool shaderparameterbool = VALUES[i];
            if (shaderparameterbool.getName().equals(str)) {
               return shaderparameterbool;
            }
         }

         return null;
      }
   }
}