package net.optifine.entity.model.anim;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.optifine.Config;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.expr.IExpressionFloat;
import net.optifine.util.MathUtils;
import net.optifine.util.WorldUtils;

public enum RenderEntityParameterFloat implements IExpressionFloat {
   LIMB_SWING("limb_swing"),
   LIMB_SWING_SPEED("limb_speed"),
   AGE("age"),
   HEAD_YAW("head_yaw"),
   HEAD_PITCH("head_pitch"),
   HEALTH("health"),
   HURT_TIME("hurt_time"),
   DEATH_TIME("death_time"),
   IDLE_TIME("idle_time"),
   MAX_HEALTH("max_health"),
   MOVE_FORWARD("move_forward"),
   MOVE_STRAFING("move_strafing"),
   POS_X("pos_x", true),
   POS_Y("pos_y", true),
   POS_Z("pos_z", true),
   ROT_X("rot_x"),
   ROT_Y("rot_y"),
   ID("id", true),
   PLAYER_POS_X("player_pos_x", true),
   PLAYER_POS_Y("player_pos_y", true),
   PLAYER_POS_Z("player_pos_z", true),
   PLAYER_ROT_X("player_rot_x", true),
   PLAYER_ROT_Y("player_rot_y", true),
   FRAME_TIME("frame_time", true),
   ANGER_TIME("anger_time"),
   ANGER_TIME_START("anger_time_start"),
   SWING_PROGRESS("swing_progress"),
   DIMENSION("dimension", true),
   RULE_INDEX("rule_index", true);

   private String name;
   private boolean blockEntity;
   private EntityRenderDispatcher renderManager;
   private static final RenderEntityParameterFloat[] VALUES = values();
   private static Minecraft mc = Minecraft.getInstance();
   private static String KEY_ANGER_TIME_MAX = "ANGER_TIME_MAX";

   private RenderEntityParameterFloat(String name) {
      this(name, false);
   }

   private RenderEntityParameterFloat(String name, boolean blockEntity) {
      this.name = name;
      this.blockEntity = blockEntity;
      this.renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
   }

   public String getName() {
      return this.name;
   }

   public boolean isBlockEntity() {
      return this.blockEntity;
   }

   public float eval() {
      switch (this) {
         case PLAYER_POS_X:
            return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)mc.player.xo, (float)mc.player.getX());
         case PLAYER_POS_Y:
            return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)mc.player.yo, (float)mc.player.getY());
         case PLAYER_POS_Z:
            return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)mc.player.zo, (float)mc.player.getZ());
         case PLAYER_ROT_X:
            return MathUtils.toRad(Mth.lerp(GameRenderer.getRenderPartialTicks(), mc.player.xRotO, mc.player.getXRot()));
         case PLAYER_ROT_Y:
            return MathUtils.toRad(Mth.lerp(GameRenderer.getRenderPartialTicks(), mc.player.yRotO, mc.player.getYRot()));
         case FRAME_TIME:
            return (float)Config.getLastFrameTimeMs() / 1000.0F;
         case DIMENSION:
            return (float)WorldUtils.getDimensionId(mc.level);
         case RULE_INDEX:
            return (float)CustomEntityModels.getMatchingRuleIndex();
         default:
            BlockEntity blockentity = BlockEntityRenderDispatcher.tileEntityRendered;
            if (blockentity != null) {
               switch (this) {
                  case POS_X:
                     return (float)blockentity.getBlockPos().getX();
                  case POS_Y:
                     return (float)blockentity.getBlockPos().getY();
                  case POS_Z:
                     return (float)blockentity.getBlockPos().getZ();
                  case ID:
                     return this.toFloat(blockentity.getBlockPos());
               }
            }

            EntityRenderer entityrenderer = this.renderManager.getEntityRenderer();
            if (entityrenderer == null) {
               return 0.0F;
            } else {
               Entity entity = this.renderManager.getRenderedEntity();
               if (entity == null) {
                  return 0.0F;
               } else {
                  if (entityrenderer instanceof LivingEntityRenderer) {
                     LivingEntityRenderer livingentityrenderer = (LivingEntityRenderer)entityrenderer;
                     switch (this) {
                        case LIMB_SWING:
                           return livingentityrenderer.renderLimbSwing;
                        case LIMB_SWING_SPEED:
                           return livingentityrenderer.renderLimbSwingAmount;
                        case AGE:
                           return livingentityrenderer.renderAgeInTicks;
                        case HEAD_YAW:
                           return livingentityrenderer.renderHeadYaw;
                        case HEAD_PITCH:
                           return livingentityrenderer.renderHeadPitch;
                        default:
                           if (entity instanceof LivingEntity) {
                              LivingEntity livingentity = (LivingEntity)entity;
                              switch (this) {
                                 case HEALTH:
                                    return livingentity.getHealth();
                                 case HURT_TIME:
                                    return livingentity.hurtTime > 0 ? (float)livingentity.hurtTime - GameRenderer.getRenderPartialTicks() : 0.0F;
                                 case DEATH_TIME:
                                    return livingentity.deathTime > 0 ? (float)livingentity.deathTime + GameRenderer.getRenderPartialTicks() : 0.0F;
                                 case IDLE_TIME:
                                    return livingentity.getNoActionTime() > 0 ? (float)livingentity.getNoActionTime() + GameRenderer.getRenderPartialTicks() : 0.0F;
                                 case MAX_HEALTH:
                                    return livingentity.getMaxHealth();
                                 case MOVE_FORWARD:
                                    return livingentity.zza;
                                 case MOVE_STRAFING:
                                    return livingentity.xxa;
                                 case ANGER_TIME:
                                    if (livingentity instanceof NeutralMob) {
                                       NeutralMob neutralmob = (NeutralMob)livingentity;
                                       float f = (float)neutralmob.getRemainingPersistentAngerTime();
                                       float f1 = EntityVariableFloat.getEntityValue(KEY_ANGER_TIME_MAX);
                                       if (f > 0.0F) {
                                          if (f > f1) {
                                             EntityVariableFloat.setEntityValue(KEY_ANGER_TIME_MAX, f);
                                          }

                                          if (f < f1) {
                                             f -= GameRenderer.getRenderPartialTicks();
                                          }
                                       } else if (f1 > 0.0F) {
                                          EntityVariableFloat.setEntityValue(KEY_ANGER_TIME_MAX, 0.0F);
                                       }

                                       return f;
                                    }
                                 case ANGER_TIME_START:
                                    return EntityVariableFloat.getEntityValue(KEY_ANGER_TIME_MAX);
                                 case SWING_PROGRESS:
                                    return livingentity.getAttackAnim(GameRenderer.getRenderPartialTicks());
                              }
                           }
                     }
                  }

                  if (entity instanceof Boat) {
                     Boat boat = (Boat)entity;
                     switch (this) {
                        case LIMB_SWING:
                           float f2 = boat.getRowingTime(0, GameRenderer.getRenderPartialTicks());
                           float f4 = boat.getRowingTime(1, GameRenderer.getRenderPartialTicks());
                           return Math.max(f2, f4);
                        case LIMB_SWING_SPEED:
                           return 1.0F;
                     }
                  }

                  if (entity instanceof AbstractMinecart) {
                     AbstractMinecart abstractminecart = (AbstractMinecart)entity;
                     switch (this) {
                        case LIMB_SWING:
                           float f3 = Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)abstractminecart.xOld, (float)abstractminecart.getX());
                           float f5 = Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)abstractminecart.zOld, (float)abstractminecart.getZ());
                           BlockState blockstate = Minecraft.getInstance().level.getBlockState(abstractminecart.blockPosition());
                           if (blockstate.is(BlockTags.RAILS)) {
                              RailShape railshape = blockstate.getValue(((BaseRailBlock)blockstate.getBlock()).getShapeProperty());
                              switch (railshape) {
                                 case SOUTH_WEST:
                                    return f3 + f5;
                                 case SOUTH_EAST:
                                    return -(f3 - f5);
                                 case NORTH_WEST:
                                    return f3 - f5;
                                 default:
                                    return -(f3 + f5);
                              }
                           }
                        case LIMB_SWING_SPEED:
                           return 1.0F;
                     }
                  }

                  switch (this) {
                     case POS_X:
                        return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)entity.xo, (float)entity.getX());
                     case POS_Y:
                        return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)entity.yo, (float)entity.getY());
                     case POS_Z:
                        return Mth.lerp(GameRenderer.getRenderPartialTicks(), (float)entity.zo, (float)entity.getZ());
                     case ID:
                        return this.toFloat(entity.getUUID());
                     case ROT_X:
                        return MathUtils.toRad(Mth.lerp(GameRenderer.getRenderPartialTicks(), entity.xRotO, entity.getXRot()));
                     case ROT_Y:
                        if (entity instanceof LivingEntity) {
                           return MathUtils.toRad(Mth.lerp(GameRenderer.getRenderPartialTicks(), ((LivingEntity)entity).yBodyRotO, ((LivingEntity)entity).yBodyRot));
                        }

                        return MathUtils.toRad(Mth.lerp(GameRenderer.getRenderPartialTicks(), entity.yRotO, entity.getYRot()));
                     default:
                        return 0.0F;
                  }
               }
            }
      }
   }

   private float toFloat(UUID uuid) {
      int i = Long.hashCode(uuid.getLeastSignificantBits());
      int j = Long.hashCode(uuid.getMostSignificantBits());
      return Float.intBitsToFloat(i ^ j);
   }

   private float toFloat(BlockPos pos) {
      int i = Config.getRandom(pos, 0);
      return Float.intBitsToFloat(i);
   }

   public static RenderEntityParameterFloat parse(String str) {
      if (str == null) {
         return null;
      } else {
         for(int i = 0; i < VALUES.length; ++i) {
            RenderEntityParameterFloat renderentityparameterfloat = VALUES[i];
            if (renderentityparameterfloat.getName().equals(str)) {
               return renderentityparameterfloat;
            }
         }

         return null;
      }
   }
}