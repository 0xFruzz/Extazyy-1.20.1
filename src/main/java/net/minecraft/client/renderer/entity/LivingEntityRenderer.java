package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.Team;
import net.optifine.Config;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import org.slf4j.Logger;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.util.Mine;

public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float EYE_BED_OFFSET = 0.1F;
   public M model;
   protected final List<RenderLayer<T, M>> layers = Lists.newArrayList();
   public float renderLimbSwing;
   public float renderLimbSwingAmount;
   public float renderAgeInTicks;
   public float renderHeadYaw;
   public float renderHeadPitch;
   public static final boolean animateModelLiving = Boolean.getBoolean("animate.model.living");
   private static boolean renderItemHead = false;

   public LivingEntityRenderer(EntityRendererProvider.Context pContext, M pModel, float pShadowRadius) {
      super(pContext);
      this.model = pModel;
      this.shadowRadius = pShadowRadius;
   }

   public final boolean addLayer(RenderLayer<T, M> pLayer) {
      return this.layers.add(pLayer);
   }

   public M getModel() {
      return this.model;
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      if (!Reflector.RenderLivingEvent_Pre_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Pre_Constructor, pEntity, this, pPartialTicks, pPoseStack, pBuffer, pPackedLight)) {
         if (animateModelLiving) {
            pEntity.walkAnimation.setSpeed(1.5F);
         }

         pPoseStack.pushPose();
         this.model.attackTime = this.getAttackAnim(pEntity, pPartialTicks);
         this.model.riding = pEntity.isPassenger();
         if (Reflector.IForgeEntity_shouldRiderSit.exists()) {
            this.model.riding = pEntity.isPassenger() && pEntity.getVehicle() != null && Reflector.callBoolean(pEntity.getVehicle(), Reflector.IForgeEntity_shouldRiderSit);
         }

         if(Extazyy.moduleManager.babyMode.state) {
            if(Extazyy.moduleManager.babyMode.here.get()) {
               if(pEntity.getScoreboardName().equals(Mine.mc.player.getScoreboardName())) {
                  this.model.young = true;
               } else {
                  this.model.young = pEntity.isBaby();
               }
            } else {
               this.model.young = true;
            }
         } else {
            this.model.young = pEntity.isBaby();
         }
         float f = Mth.rotLerp(pPartialTicks, pEntity.yBodyRotO, pEntity.yBodyRot);
         float f1;
         if(pEntity instanceof LocalPlayer e) {
           // f = e.getRotationEvent().getYRot();
            f1 = e.getRotationEvent().getYRot();
         } else {
           // f = Mth.rotLerp(pPartialTicks, pEntity.yBodyRotO, pEntity.yBodyRot);
            f1 = Mth.rotLerp(pPartialTicks, pEntity.yHeadRotO, pEntity.yHeadRot);
         }

         float f2 = f1 - f;
         if (this.model.riding && pEntity.getVehicle() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)pEntity.getVehicle();
            f = Mth.rotLerp(pPartialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
            f2 = f1 - f;
            float f3 = Mth.wrapDegrees(f2);
            if (f3 < -85.0F) {
               f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
               f3 = 85.0F;
            }

            f = f1 - f3;
            if (f3 * f3 > 2500.0F) {
               f += f3 * 0.2F;
            }

            f2 = f1 - f;
         }
         float f7;
         if(pEntity instanceof LocalPlayer e) {
            f7 = e.getRotationEvent().getXRot();
         } else {
            f7 = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());
         }

         if (isEntityUpsideDown(pEntity)) {
            f7 *= -1.0F;
            f2 *= -1.0F;
         }

         if (pEntity.hasPose(Pose.SLEEPING)) {
            Direction direction = pEntity.getBedOrientation();
            if (direction != null) {
               float f4 = pEntity.getEyeHeight(Pose.STANDING) - 0.1F;
               pPoseStack.translate((float)(-direction.getStepX()) * f4, 0.0F, (float)(-direction.getStepZ()) * f4);
            }
         }

         float f8 = this.getBob(pEntity, pPartialTicks);
         this.setupRotations(pEntity, pPoseStack, f8, f, pPartialTicks);
         pPoseStack.scale(-1.0F, -1.0F, 1.0F);
         this.scale(pEntity, pPoseStack, pPartialTicks);
         pPoseStack.translate(0.0F, -1.501F, 0.0F);
         float f9 = 0.0F;
         float f5 = 0.0F;
         if (!pEntity.isPassenger() && pEntity.isAlive()) {
            f9 = pEntity.walkAnimation.speed(pPartialTicks);
            f5 = pEntity.walkAnimation.position(pPartialTicks);
            if (pEntity.isBaby()) {
               f5 *= 3.0F;
            }

            if (f9 > 1.0F) {
               f9 = 1.0F;
            }
         }

         this.model.prepareMobModel(pEntity, f5, f9, pPartialTicks);
         this.model.setupAnim(pEntity, f5, f9, f8, f2, f7);
         if (CustomEntityModels.isActive()) {
            this.renderLimbSwing = f5;
            this.renderLimbSwingAmount = f9;
            this.renderAgeInTicks = f8;
            this.renderHeadYaw = f2;
            this.renderHeadPitch = f7;
         }

         boolean flag = Config.isShaders();
         Minecraft minecraft = Minecraft.getInstance();
         boolean flag1 = this.isBodyVisible(pEntity);
         boolean flag2 = !flag1 && !pEntity.isInvisibleTo(minecraft.player);
         boolean flag3 = minecraft.shouldEntityAppearGlowing(pEntity);
         RenderType rendertype = this.getRenderType(pEntity, flag1, flag2, flag3);
         if (rendertype != null) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(rendertype);
            float f6 = this.getWhiteOverlayProgress(pEntity, pPartialTicks);
            if (flag) {
               if (pEntity.hurtTime > 0 || pEntity.deathTime > 0) {
                  Shaders.setEntityColor(1.0F, 0.0F, 0.0F, 0.3F);
               }

               if (f6 > 0.0F) {
                  Shaders.setEntityColor(f6, f6, f6, 0.5F);
               }
            }

            int i = getOverlayCoords(pEntity, f6);
            this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, i, 1.0F, 1.0F, 1.0F, flag2 ? 0.15F : 1.0F);
         }

         if (!pEntity.isSpectator()) {
            for(RenderLayer<T, M> renderlayer : this.layers) {
               if (renderlayer instanceof CustomHeadLayer) {
                  renderItemHead = true;
               }

               renderlayer.render(pPoseStack, pBuffer, pPackedLight, pEntity, f5, f9, pPartialTicks, f8, f2, f7);
               renderItemHead = false;
            }
         }

         if (Config.isShaders()) {
            Shaders.setEntityColor(0.0F, 0.0F, 0.0F, 0.0F);
         }

         pPoseStack.popPose();
         pPoseStack.pushPose();
         pPoseStack.translate(0,getModel().young ? -0.9 : 0,0);
         super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
         pPoseStack.popPose();
         if (Reflector.RenderLivingEvent_Post_Constructor.exists()) {
            Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Post_Constructor, pEntity, this, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
         }

      }
   }

   @Nullable
   protected RenderType getRenderType(T pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing) {
      ResourceLocation resourcelocation = this.getTextureLocation(pLivingEntity);
      if (this.getLocationTextureCustom() != null) {
         resourcelocation = this.getLocationTextureCustom();
      }

      if (pTranslucent) {
         return RenderType.itemEntityTranslucentCull(resourcelocation);
      } else if (pBodyVisible) {
         return this.model.renderType(resourcelocation);
      } else if (pGlowing && !Config.getMinecraft().levelRenderer.shouldShowEntityOutlines()) {
         return this.model.renderType(resourcelocation);
      } else {
         return pGlowing ? RenderType.outline(resourcelocation) : null;
      }
   }

   public static int getOverlayCoords(LivingEntity pLivingEntity, float pU) {
      return OverlayTexture.pack(OverlayTexture.u(pU), OverlayTexture.v(pLivingEntity.hurtTime > 0 || pLivingEntity.deathTime > 0));
   }

   protected boolean isBodyVisible(T pLivingEntity) {
      return !pLivingEntity.isInvisible();
   }

   private static float sleepDirectionToRotation(Direction pFacing) {
      switch (pFacing) {
         case SOUTH:
            return 90.0F;
         case WEST:
            return 0.0F;
         case NORTH:
            return 270.0F;
         case EAST:
            return 180.0F;
         default:
            return 0.0F;
      }
   }

   protected boolean isShaking(T pEntity) {
      return pEntity.isFullyFrozen();
   }

   protected void setupRotations(T pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      if (this.isShaking(pEntityLiving)) {
         pRotationYaw += (float)(Math.cos((double)pEntityLiving.tickCount * 3.25D) * Math.PI * (double)0.4F);
      }

      if (!pEntityLiving.hasPose(Pose.SLEEPING)) {
         pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - pRotationYaw));
      }

      if (pEntityLiving.deathTime > 0) {
         float f = ((float)pEntityLiving.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
         f = Mth.sqrt(f);
         if (f > 1.0F) {
            f = 1.0F;
         }

         pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * this.getFlipDegrees(pEntityLiving)));
      } else if (pEntityLiving.isAutoSpinAttack()) {
         pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - pEntityLiving.getXRot()));
         pPoseStack.mulPose(Axis.YP.rotationDegrees(((float)pEntityLiving.tickCount + pPartialTicks) * -75.0F));
      } else if (pEntityLiving.hasPose(Pose.SLEEPING)) {
         Direction direction = pEntityLiving.getBedOrientation();
         float f1 = direction != null ? sleepDirectionToRotation(direction) : pRotationYaw;
         pPoseStack.mulPose(Axis.YP.rotationDegrees(f1));
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees(pEntityLiving)));
         pPoseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
      } else if (isEntityUpsideDown(pEntityLiving)) {
         pPoseStack.translate(0.0F, pEntityLiving.getBbHeight() + 0.1F, 0.0F);
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
      }

   }

   protected float getAttackAnim(T pLivingBase, float pPartialTickTime) {
      return pLivingBase.getAttackAnim(pPartialTickTime);
   }

   protected float getBob(T pLivingBase, float pPartialTick) {
      return (float)pLivingBase.tickCount + pPartialTick;
   }

   protected float getFlipDegrees(T pLivingEntity) {
      return 90.0F;
   }

   protected float getWhiteOverlayProgress(T pLivingEntity, float pPartialTicks) {
      return 0.0F;
   }

   protected void scale(T pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
   }

   protected boolean shouldShowName(T pEntity) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
      float f = pEntity.isDiscrete() ? 32.0F : 64.0F;
      if (d0 >= (double)(f * f)) {
         return false;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer localplayer = minecraft.player;
         boolean flag = !pEntity.isInvisibleTo(localplayer);
         if (pEntity != localplayer) {
            Team team = pEntity.getTeam();
            Team team1 = localplayer.getTeam();
            if (team != null) {
               Team.Visibility team$visibility = team.getNameTagVisibility();
               switch (team$visibility) {
                  case ALWAYS:
                     return flag;
                  case NEVER:
                     return false;
                  case HIDE_FOR_OTHER_TEAMS:
                     return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);
                  case HIDE_FOR_OWN_TEAM:
                     return team1 == null ? flag : !team.isAlliedTo(team1) && flag;
                  default:
                     return true;
               }
            }
         }

         return Minecraft.renderNames() && pEntity != minecraft.getCameraEntity() && flag && !pEntity.isVehicle();
      }
   }

   public static boolean isEntityUpsideDown(LivingEntity pEntity) {
      if (pEntity instanceof Player || pEntity.hasCustomName()) {
         String s = ChatFormatting.stripFormatting(pEntity.getName().getString());
         if ("Dinnerbone".equals(s) || "Grumm".equals(s) || "Fruzz".equals(s)) {
            return !(pEntity instanceof Player) || ((Player)pEntity).isModelPartShown(PlayerModelPart.CAPE);
         }
      }

      return false;
   }

   public <T extends RenderLayer> T getLayer(Class<T> cls) {
      List<T> list = this.getLayers(cls);
      return (T)(list.isEmpty() ? null : list.get(0));
   }

   public <T extends RenderLayer> List<T> getLayers(Class<T> cls) {
      List<RenderLayer> list = new ArrayList<>();

      for(RenderLayer renderlayer : this.layers) {
         if (cls.isInstance(renderlayer)) {
            list.add(renderlayer);
         }
      }

      return (List<T>) list;
   }

   public void removeLayers(Class cls) {
      Iterator iterator = this.layers.iterator();

      while(iterator.hasNext()) {
         RenderLayer renderlayer = (RenderLayer)iterator.next();
         if (cls.isInstance(renderlayer)) {
            iterator.remove();
         }
      }

   }

   public static boolean isRenderItemHead() {
      return renderItemHead;
   }
}
