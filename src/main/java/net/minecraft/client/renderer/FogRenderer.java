package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.Vec3M;
import net.optifine.reflect.Reflector;
import net.optifine.render.GLConst;
import net.optifine.shaders.Shaders;
import org.joml.Vector3f;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.main.modules.impl.render.CustomWorld;
import ru.fruzz.extazyy.misc.util.color.ColorUtils;

public class FogRenderer {
   private static final int WATER_FOG_DISTANCE = 96;
   private static final List<FogRenderer.MobEffectFogFunction> MOB_EFFECT_FOG = Lists.newArrayList(new FogRenderer.BlindnessFogFunction(), new FogRenderer.DarknessFogFunction());
   public static final float BIOME_FOG_TRANSITION_TIME = 5000.0F;
   public static float fogRed;
   public static float fogGreen;
   public static float fogBlue;
   private static int targetBiomeFog = -1;
   private static int previousBiomeFog = -1;
   private static long biomeChangedTime = -1L;
   public static boolean fogStandard = false;

   public static void setupColor(Camera pActiveRenderInfo, float pPartialTicks, ClientLevel pLevel, int pRenderDistanceChunks, float pBossColorModifier) {
      FogType fogtype = pActiveRenderInfo.getFluidInCamera();
      Entity entity = pActiveRenderInfo.getEntity();

      if (fogtype == FogType.WATER) {
         long i = Util.getMillis();
         int j = pLevel.getBiome(BlockPos.containing(pActiveRenderInfo.getPosition())).value().getWaterFogColor();
         if (biomeChangedTime < 0L) {
            targetBiomeFog = j;
            previousBiomeFog = j;
            biomeChangedTime = i;
         }

         int k = targetBiomeFog >> 16 & 255;
         int l = targetBiomeFog >> 8 & 255;
         int i1 = targetBiomeFog & 255;
         int j1 = previousBiomeFog >> 16 & 255;
         int k1 = previousBiomeFog >> 8 & 255;
         int l1 = previousBiomeFog & 255;
         float f = Mth.clamp((float)(i - biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
         float f1 = Mth.lerp(f, (float)j1, (float)k);
         float f2 = Mth.lerp(f, (float)k1, (float)l);
         float f3 = Mth.lerp(f, (float)l1, (float)i1);
         fogRed = f1 / 255.0F;
         fogGreen = f2 / 255.0F;
         fogBlue = f3 / 255.0F;
         if (targetBiomeFog != j) {
            targetBiomeFog = j;
            previousBiomeFog = Mth.floor(f1) << 16 | Mth.floor(f2) << 8 | Mth.floor(f3);
            biomeChangedTime = i;
         }
      } else if (fogtype == FogType.LAVA) {
         fogRed = 0.6F;
         fogGreen = 0.1F;
         fogBlue = 0.0F;
         biomeChangedTime = -1L;
      } else if (fogtype == FogType.POWDER_SNOW) {
         fogRed = 0.623F;
         fogGreen = 0.734F;
         fogBlue = 0.785F;
         biomeChangedTime = -1L;
         RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
      } else {
         float f4 = 0.25F + 0.75F * (float)pRenderDistanceChunks / 32.0F;
         f4 = 1.0F - (float)Math.pow((double)f4, 0.25D);
         Vec3 vec3 = pLevel.getSkyColor(pActiveRenderInfo.getPosition(), pPartialTicks);
         vec3 = CustomColors.getWorldSkyColor(vec3, pLevel, pActiveRenderInfo.getEntity(), pPartialTicks);
         float f6 = (float)vec3.x;
         float f8 = (float)vec3.y;
         float f10 = (float)vec3.z;
         float f11 = Mth.clamp(Mth.cos(pLevel.getTimeOfDay(pPartialTicks) * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
         BiomeManager biomemanager = pLevel.getBiomeManager();
         Vec3 vec33 = pActiveRenderInfo.getPosition().subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
         Vec3M vec3m = new Vec3M(0.0D, 0.0D, 0.0D);
         Vec3 vec34 = CubicSampler.sampleM(vec33, (xIn, yIn, zIn) -> {
            return vec3m.fromRgb(biomemanager.getNoiseBiomeAtQuart(xIn, yIn, zIn).value().getFogColor());
         });
         vec34 = pLevel.effects().getBrightnessDependentFogColor(vec34, f11);
         vec34 = CustomColors.getWorldFogColor(vec34, pLevel, pActiveRenderInfo.getEntity(), pPartialTicks);
         fogRed = (float)vec34.x();
         fogGreen = (float)vec34.y();
         fogBlue = (float)vec34.z();

         if (pRenderDistanceChunks >= 4) {
            float f12 = Mth.sin(pLevel.getSunAngle(pPartialTicks)) > 0.0F ? -1.0F : 1.0F;
            Vector3f vector3f1 = new Vector3f(f12, 0.0F, 0.0F);
            float f16 = pActiveRenderInfo.getLookVector().dot(vector3f1);
            if (f16 < 0.0F) {
               f16 = 0.0F;
            }

            if (f16 > 0.0F) {
               float[] afloat = pLevel.effects().getSunriseColor(pLevel.getTimeOfDay(pPartialTicks), pPartialTicks);
               if (afloat != null) {
                  f16 *= afloat[3];
                  fogRed = fogRed * (1.0F - f16) + afloat[0] * f16;
                  fogGreen = fogGreen * (1.0F - f16) + afloat[1] * f16;
                  fogBlue = fogBlue * (1.0F - f16) + afloat[2] * f16;
               }
            }
         }

         fogRed += (f6 - fogRed) * f4;
         fogGreen += (f8 - fogGreen) * f4;
         fogBlue += (f10 - fogBlue) * f4;
         float f13 = pLevel.getRainLevel(pPartialTicks);
         if (f13 > 0.0F) {
            float f14 = 1.0F - f13 * 0.5F;
            float f17 = 1.0F - f13 * 0.4F;
            fogRed *= f14;
            fogGreen *= f14;
            fogBlue *= f17;
         }

         float f15 = pLevel.getThunderLevel(pPartialTicks);
         if (f15 > 0.0F) {
            float f18 = 1.0F - f15 * 0.5F;
            fogRed *= f18;
            fogGreen *= f18;
            fogBlue *= f18;
         }

         biomeChangedTime = -1L;
      }

      float f5 = ((float)pActiveRenderInfo.getPosition().y - (float)pLevel.getMinBuildHeight()) * pLevel.getLevelData().getClearColorScale();
      FogRenderer.MobEffectFogFunction fogrenderer$mobeffectfogfunction = getPriorityFogFunction(entity, pPartialTicks);
      if (fogrenderer$mobeffectfogfunction != null) {
         LivingEntity livingentity = (LivingEntity)entity;
         f5 = fogrenderer$mobeffectfogfunction.getModifiedVoidDarkness(livingentity, livingentity.getEffect(fogrenderer$mobeffectfogfunction.getMobEffect()), f5, pPartialTicks);
      }

      if (f5 < 1.0F && fogtype != FogType.LAVA && fogtype != FogType.POWDER_SNOW) {
         if (f5 < 0.0F) {
            f5 = 0.0F;
         }

         f5 *= f5;
         fogRed *= f5;
         fogGreen *= f5;
         fogBlue *= f5;
      }

      if (pBossColorModifier > 0.0F) {
         fogRed = fogRed * (1.0F - pBossColorModifier) + fogRed * 0.7F * pBossColorModifier;
         fogGreen = fogGreen * (1.0F - pBossColorModifier) + fogGreen * 0.6F * pBossColorModifier;
         fogBlue = fogBlue * (1.0F - pBossColorModifier) + fogBlue * 0.6F * pBossColorModifier;
      }

      float f7;
      if (fogtype == FogType.WATER) {
         if (entity instanceof LocalPlayer) {
            f7 = ((LocalPlayer)entity).getWaterVision();
         } else {
            f7 = 1.0F;
         }
      } else {
         label102: {
            if (entity instanceof LivingEntity) {
               LivingEntity livingentity1 = (LivingEntity)entity;
               if (livingentity1.hasEffect(MobEffects.NIGHT_VISION) && !livingentity1.hasEffect(MobEffects.DARKNESS)) {
                  f7 = GameRenderer.getNightVisionScale(livingentity1, pPartialTicks);
                  break label102;
               }
            }

            f7 = 0.0F;
         }
      }

      if (fogRed != 0.0F && fogGreen != 0.0F && fogBlue != 0.0F) {
         float f9 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
         fogRed = fogRed * (1.0F - f7) + fogRed * f9 * f7;
         fogGreen = fogGreen * (1.0F - f7) + fogGreen * f9 * f7;
         fogBlue = fogBlue * (1.0F - f7) + fogBlue * f9 * f7;
      }

      if (fogtype == FogType.WATER) {
         Entity entity1 = pActiveRenderInfo.getEntity();
         Vec3 vec31 = CustomColors.getUnderwaterColor(pLevel, entity1.getX(), entity1.getY() + 1.0D, entity1.getZ());
         if (vec31 != null) {
            fogRed = (float)vec31.x;
            fogGreen = (float)vec31.y;
            fogBlue = (float)vec31.z;
         }
      } else if (fogtype == FogType.LAVA) {
         Entity entity2 = pActiveRenderInfo.getEntity();
         Vec3 vec32 = CustomColors.getUnderlavaColor(pLevel, entity2.getX(), entity2.getY() + 1.0D, entity2.getZ());
         if (vec32 != null) {
            fogRed = (float)vec32.x;
            fogGreen = (float)vec32.y;
            fogBlue = (float)vec32.z;
         }
      }

      if (Reflector.ForgeHooksClient_getFogColor.exists()) {
         Vector3f vector3f = (Vector3f)Reflector.ForgeHooksClient_getFogColor.call(pActiveRenderInfo, pPartialTicks, pLevel, pRenderDistanceChunks, pBossColorModifier, fogRed, fogGreen, fogBlue);
         fogRed = vector3f.x();
         fogGreen = vector3f.y();
         fogBlue = vector3f.z();
      }
      Shaders.setClearColor(fogRed, fogGreen, fogBlue, 0.0F);
      RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
   }

   public static void setupNoFog() {
      RenderSystem.setShaderFogStart(Float.MAX_VALUE);
      if (Config.isShaders()) {
         Shaders.setFogDensity(0.0F);
         Shaders.setFogMode(GLConst.GL_EXP2);
         Shaders.setFogStart(1.7014117E38F);
         Shaders.setFogEnd(Float.MAX_VALUE);
      }

   }

   @Nullable
   private static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity pEntity, float p_234167_) {
      if (pEntity instanceof LivingEntity livingentity) {
         return MOB_EFFECT_FOG.stream().filter((p_234168_2_) -> {
            return p_234168_2_.isEnabled(livingentity, p_234167_);
         }).findFirst().orElse((FogRenderer.MobEffectFogFunction)null);
      } else {
         return null;
      }
   }

   public static void setupFog(Camera pCamera, FogRenderer.FogMode pFogMode, float pFarPlaneDistance, boolean p_234176_, float p_234177_) {
      fogStandard = false;
      FogType fogtype = pCamera.getFluidInCamera();
      Entity entity = pCamera.getEntity();
      FogRenderer.FogData fogrenderer$fogdata = new FogRenderer.FogData(pFogMode);
      FogRenderer.MobEffectFogFunction fogrenderer$mobeffectfogfunction = getPriorityFogFunction(entity, p_234177_);
      if (fogtype == FogType.LAVA) {
         if (entity.isSpectator()) {
            fogrenderer$fogdata.start = -8.0F;
            fogrenderer$fogdata.end = pFarPlaneDistance * 0.5F;
         } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
            fogrenderer$fogdata.start = 0.0F;
            fogrenderer$fogdata.end = 3.0F;
         } else {
            fogrenderer$fogdata.start = 0.25F;
            fogrenderer$fogdata.end = 1.0F;
         }
      } else if (fogtype == FogType.POWDER_SNOW) {
         if (entity.isSpectator()) {
            fogrenderer$fogdata.start = -8.0F;
            fogrenderer$fogdata.end = pFarPlaneDistance * 0.5F;
         } else {
            fogrenderer$fogdata.start = 0.0F;
            fogrenderer$fogdata.end = 2.0F;
         }
      } else if (fogrenderer$mobeffectfogfunction != null) {
         LivingEntity livingentity = (LivingEntity)entity;
         MobEffectInstance mobeffectinstance = livingentity.getEffect(fogrenderer$mobeffectfogfunction.getMobEffect());
         if (mobeffectinstance != null) {
            fogrenderer$mobeffectfogfunction.setupFog(fogrenderer$fogdata, livingentity, mobeffectinstance, pFarPlaneDistance, p_234177_);
         }
      } else if (fogtype == FogType.WATER) {
         fogrenderer$fogdata.start = -8.0F;
         fogrenderer$fogdata.end = 96.0F;
         if (entity instanceof LocalPlayer) {
            LocalPlayer localplayer = (LocalPlayer)entity;
            fogrenderer$fogdata.end *= Math.max(0.25F, localplayer.getWaterVision());
            Holder<Biome> holder = localplayer.level().getBiome(localplayer.blockPosition());
            if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
               fogrenderer$fogdata.end *= 0.85F;
            }
         }

         if (fogrenderer$fogdata.end > pFarPlaneDistance) {
            fogrenderer$fogdata.end = pFarPlaneDistance;
            fogrenderer$fogdata.shape = FogShape.CYLINDER;
         }
      } else if (p_234176_) {
         fogStandard = true;
         fogrenderer$fogdata.start = pFarPlaneDistance * 0.05F;
         fogrenderer$fogdata.end = Math.min(pFarPlaneDistance, 192.0F) * 0.5F;
      } else if (pFogMode == FogRenderer.FogMode.FOG_SKY) {

         fogrenderer$fogdata.start = 0.0F;
         fogrenderer$fogdata.end = Math.min(pFarPlaneDistance, 512.0F);
         fogrenderer$fogdata.shape = FogShape.CYLINDER;
      } else {
         fogStandard = true;
         float f = Mth.clamp(pFarPlaneDistance / 10.0F, 4.0F, 64.0F);
         fogrenderer$fogdata.start = pFarPlaneDistance * Config.getFogStart();
         fogrenderer$fogdata.end = pFarPlaneDistance;
         fogrenderer$fogdata.shape = FogShape.CYLINDER;
      }

      RenderSystem.setShaderFogStart(fogrenderer$fogdata.start);
      RenderSystem.setShaderFogEnd(fogrenderer$fogdata.end);
      RenderSystem.setShaderFogShape(fogrenderer$fogdata.shape);
      if (Config.isShaders()) {
         Shaders.setFogStart(fogrenderer$fogdata.start);
         Shaders.setFogEnd(fogrenderer$fogdata.end);
         Shaders.setFogMode(9729);
         Shaders.setFogShape(fogrenderer$fogdata.shape.ordinal());
      }

      if (Reflector.ForgeHooksClient_onFogRender.exists()) {
         Reflector.callVoid(Reflector.ForgeHooksClient_onFogRender, pFogMode, fogtype, pCamera, p_234177_, pFarPlaneDistance, fogrenderer$fogdata.start, fogrenderer$fogdata.end, fogrenderer$fogdata.shape);
      }
      CustomWorld world = Extazyy.moduleManager.customWorld;
      if(world.state && world.modes.get(1)) {
         RenderSystem.setShaderFogStart(3);
         RenderSystem.setShaderFogEnd(world.distanceFog.getValue().floatValue() * 3);
         if(world.theme.get()) {
            RenderSystem.setShaderFogColor(ColorUtils.getRed(ColorUtils.getColorStyle(0)) / 255f,ColorUtils.getGreen(ColorUtils.getColorStyle(0)) / 255f,ColorUtils.getBlue(ColorUtils.getColorStyle(0)) / 255f);
            //CUSTOM FOG
         } else {
            RenderSystem.setShaderFogColor(ColorUtils.getRed(world.colorFog.color) / 255f,ColorUtils.getGreen(world.colorFog.color) / 255f,ColorUtils.getBlue(world.colorFog.color) / 255f);

         }
      }
   }

   public static void levelFogColor() {
      RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);
      if (Config.isShaders()) {
         Shaders.setFogColor(0, 0, 0);
      }

   }

   static class BlindnessFogFunction implements FogRenderer.MobEffectFogFunction {
      public MobEffect getMobEffect() {
         return MobEffects.BLINDNESS;
      }

      public void setupFog(FogRenderer.FogData p_234181_, LivingEntity p_234182_, MobEffectInstance p_234183_, float p_234184_, float p_234185_) {
         float f = p_234183_.isInfiniteDuration() ? 5.0F : Mth.lerp(Math.min(1.0F, (float)p_234183_.getDuration() / 20.0F), p_234184_, 5.0F);
         if (p_234181_.mode == FogRenderer.FogMode.FOG_SKY) {
            p_234181_.start = 0.0F;
            p_234181_.end = f * 0.8F;
         } else {
            p_234181_.start = f * 0.25F;
            p_234181_.end = f;
         }

      }
   }

   static class DarknessFogFunction implements FogRenderer.MobEffectFogFunction {
      public MobEffect getMobEffect() {
         return MobEffects.DARKNESS;
      }

      public void setupFog(FogRenderer.FogData p_234194_, LivingEntity p_234195_, MobEffectInstance p_234196_, float p_234197_, float p_234198_) {
         if (!p_234196_.getFactorData().isEmpty()) {
            float f = Mth.lerp(p_234196_.getFactorData().get().getFactor(p_234195_, p_234198_), p_234197_, 15.0F);
            p_234194_.start = p_234194_.mode == FogRenderer.FogMode.FOG_SKY ? 0.0F : f * 0.75F;
            p_234194_.end = f;
         }

      }

      public float getModifiedVoidDarkness(LivingEntity p_234189_, MobEffectInstance p_234190_, float p_234191_, float p_234192_) {
         return p_234190_.getFactorData().isEmpty() ? 0.0F : 1.0F - p_234190_.getFactorData().get().getFactor(p_234189_, p_234192_);
      }
   }

   static class FogData {
      public final FogRenderer.FogMode mode;
      public float start;
      public float end;
      public FogShape shape = FogShape.SPHERE;

      public FogData(FogRenderer.FogMode pMode) {
         this.mode = pMode;
      }
   }

   public static enum FogMode {
      FOG_SKY,
      FOG_TERRAIN;
   }

   interface MobEffectFogFunction {
      MobEffect getMobEffect();

      void setupFog(FogRenderer.FogData pFogData, LivingEntity pEntity, MobEffectInstance pEffectInstance, float pFarPlaneDistance, float p_234216_);

      default boolean isEnabled(LivingEntity pEntity, float p_234207_) {
         return pEntity.hasEffect(this.getMobEffect());
      }

      default float getModifiedVoidDarkness(LivingEntity pEntity, MobEffectInstance pEffectInstance, float p_234210_, float pPartialTick) {
         MobEffectInstance mobeffectinstance = pEntity.getEffect(this.getMobEffect());
         if (mobeffectinstance != null) {
            if (mobeffectinstance.endsWithin(19)) {
               p_234210_ = 1.0F - (float)mobeffectinstance.getDuration() / 20.0F;
            } else {
               p_234210_ = 0.0F;
            }
         }

         return p_234210_;
      }
   }
}