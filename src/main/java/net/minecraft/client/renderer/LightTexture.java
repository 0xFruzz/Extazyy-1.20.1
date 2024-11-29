package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import net.optifine.util.TextureUtils;
import org.joml.Vector3f;

public class LightTexture implements AutoCloseable {
   public static final int FULL_BRIGHT = 15728880;
   public static final int FULL_SKY = 15728640;
   public static final int FULL_BLOCK = 240;
   private final DynamicTexture lightTexture;
   private final NativeImage lightPixels;
   private final ResourceLocation lightTextureLocation;
   private boolean updateLightTexture;
   private float blockLightRedFlicker;
   private final GameRenderer renderer;
   private final Minecraft minecraft;
   private boolean allowed = true;
   private boolean custom = false;
   private Vector3f tempVector = new Vector3f();
   public static final int MAX_BRIGHTNESS = pack(15, 15);
   public static final int VANILLA_EMISSIVE_BRIGHTNESS = 15794417;

   public LightTexture(GameRenderer pRenderer, Minecraft pMinecraft) {
      this.renderer = pRenderer;
      this.minecraft = pMinecraft;
      this.lightTexture = new DynamicTexture(16, 16, false);
      this.lightTextureLocation = this.minecraft.getTextureManager().register("light_map", this.lightTexture);
      this.lightPixels = this.lightTexture.getPixels();

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            this.lightPixels.setPixelRGBA(j, i, -1);
         }
      }

      this.lightTexture.upload();
   }

   public void close() {
      this.lightTexture.close();
   }

   public void tick() {
      this.blockLightRedFlicker += (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1D);
      this.blockLightRedFlicker *= 0.9F;
      this.updateLightTexture = true;
   }

   public void turnOffLightLayer() {
      RenderSystem.setShaderTexture(2, 0);
      if (Config.isShaders()) {
         Shaders.disableLightmap();
      }

   }

   public void turnOnLightLayer() {
      if (!this.allowed) {
         RenderSystem.setShaderTexture(2, TextureUtils.WHITE_TEXTURE_LOCATION);
         this.minecraft.getTextureManager().bindForSetup(TextureUtils.WHITE_TEXTURE_LOCATION);
      } else {
         RenderSystem.setShaderTexture(2, this.lightTextureLocation);
         this.minecraft.getTextureManager().bindForSetup(this.lightTextureLocation);
      }

      RenderSystem.texParameter(3553, 10241, 9729);
      RenderSystem.texParameter(3553, 10240, 9729);
      if (Config.isShaders()) {
         Shaders.enableLightmap();
      }

   }

   private float getDarknessGamma(float pPartialTick) {
      if (this.minecraft.player.hasEffect(MobEffects.DARKNESS)) {
         MobEffectInstance mobeffectinstance = this.minecraft.player.getEffect(MobEffects.DARKNESS);
         if (mobeffectinstance != null && mobeffectinstance.getFactorData().isPresent()) {
            return mobeffectinstance.getFactorData().get().getFactor(this.minecraft.player, pPartialTick);
         }
      }

      return 0.0F;
   }

   private float calculateDarknessScale(LivingEntity pEntity, float pGamma, float pPartialTick) {
      float f = 0.45F * pGamma;
      return Math.max(0.0F, Mth.cos(((float)pEntity.tickCount - pPartialTick) * (float)Math.PI * 0.025F) * f);
   }

   public void updateLightTexture(float pPartialTicks) {
      if (this.updateLightTexture) {
         this.updateLightTexture = false;
         this.minecraft.getProfiler().push("lightTex");
         ClientLevel clientlevel = this.minecraft.level;
         if (clientlevel != null) {
            this.custom = false;
            if (Config.isCustomColors()) {
               boolean flag = this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION) || this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER);
               float f = this.getDarknessGammaFactor(pPartialTicks);
               float f1 = this.getDarknessLightFactor(clientlevel, pPartialTicks);
               float f2 = f * 0.25F + f1 * 0.75F;
               if (CustomColors.updateLightmap(clientlevel, this.blockLightRedFlicker, this.lightPixels, flag, f2, pPartialTicks)) {
                  this.lightTexture.upload();
                  this.updateLightTexture = false;
                  this.minecraft.getProfiler().pop();
                  this.custom = true;
                  return;
               }
            }

            float f12 = clientlevel.getSkyDarken(1.0F);
            float f13;
            if (clientlevel.getSkyFlashTime() > 0) {
               f13 = 1.0F;
            } else {
               f13 = f12 * 0.95F + 0.05F;
            }

            float f14 = this.minecraft.options.darknessEffectScale().get().floatValue();
            float f15 = this.getDarknessGamma(pPartialTicks) * f14;
            float f3 = this.calculateDarknessScale(this.minecraft.player, f15, pPartialTicks) * f14;
            if (Config.isShaders()) {
               Shaders.setDarknessFactor(f15);
               Shaders.setDarknessLightFactor(f3);
            }

            float f4 = this.minecraft.player.getWaterVision();
            float f5;
            if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
               f5 = GameRenderer.getNightVisionScale(this.minecraft.player, pPartialTicks);
            } else if (f4 > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
               f5 = f4;
            } else {
               f5 = 0.0F;
            }

            Vector3f vector3f = (new Vector3f(f12, f12, 1.0F)).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
            float f6 = this.blockLightRedFlicker + 1.5F;
            Vector3f vector3f1 = new Vector3f();

            for(int i = 0; i < 16; ++i) {
               for(int j = 0; j < 16; ++j) {
                  float f7 = getBrightness(clientlevel.dimensionType(), i) * f13;
                  float f8 = getBrightness(clientlevel.dimensionType(), j) * f6;
                  float f9 = f8 * ((f8 * 0.6F + 0.4F) * 0.6F + 0.4F);
                  float f10 = f8 * (f8 * f8 * 0.6F + 0.4F);
                  vector3f1.set(f8, f9, f10);
                  boolean flag1 = clientlevel.effects().forceBrightLightmap();
                  if (flag1) {
                     vector3f1.lerp(this.getTempVector3f(0.99F, 1.12F, 1.0F), 0.25F);
                     clampColor(vector3f1);
                  } else {
                     Vector3f vector3f2 = this.getTempCopy(vector3f).mul(f7);
                     vector3f1.add(vector3f2);
                     vector3f1.lerp(this.getTempVector3f(0.75F, 0.75F, 0.75F), 0.04F);
                     if (this.renderer.getDarkenWorldAmount(pPartialTicks) > 0.0F) {
                        float f11 = this.renderer.getDarkenWorldAmount(pPartialTicks);
                        Vector3f vector3f3 = this.getTempCopy(vector3f1).mul(0.7F, 0.6F, 0.6F);
                        vector3f1.lerp(vector3f3, f11);
                     }
                  }

                  if (Reflector.IForgeDimensionSpecialEffects_adjustLightmapColors.exists()) {
                     Reflector.call(clientlevel.effects(), Reflector.IForgeDimensionSpecialEffects_adjustLightmapColors, clientlevel, pPartialTicks, f12, f6, f7, j, i, vector3f1);
                  }

                  if (f5 > 0.0F) {
                     float f16 = Math.max(vector3f1.x(), Math.max(vector3f1.y(), vector3f1.z()));
                     if (f16 < 1.0F) {
                        float f18 = 1.0F / f16;
                        Vector3f vector3f5 = this.getTempCopy(vector3f1).mul(f18);
                        vector3f1.lerp(vector3f5, f5);
                     }
                  }

                  if (!flag1) {
                     if (f3 > 0.0F) {
                        vector3f1.add(-f3, -f3, -f3);
                     }

                     clampColor(vector3f1);
                  }

                  float f17 = this.minecraft.options.gamma().get().floatValue();
                  Vector3f vector3f4 = this.getTempVector3f(this.notGamma(vector3f1.x), this.notGamma(vector3f1.y), this.notGamma(vector3f1.z));
                  vector3f1.lerp(vector3f4, Math.max(0.0F, f17 - f15));
                  vector3f1.lerp(this.getTempVector3f(0.75F, 0.75F, 0.75F), 0.04F);
                  clampColor(vector3f1);
                  vector3f1.mul(255.0F);
                  int j1 = 255;
                  int k = (int)vector3f1.x();
                  int l = (int)vector3f1.y();
                  int i1 = (int)vector3f1.z();
                  this.lightPixels.setPixelRGBA(j, i, -16777216 | i1 << 16 | l << 8 | k);
               }
            }

            this.lightTexture.upload();
            this.minecraft.getProfiler().pop();
         }
      }

   }

   private static void clampColor(Vector3f pColor) {
      pColor.set(Mth.clamp(pColor.x, 0.0F, 1.0F), Mth.clamp(pColor.y, 0.0F, 1.0F), Mth.clamp(pColor.z, 0.0F, 1.0F));
   }

   private float notGamma(float pValue) {
      float f = 1.0F - pValue;
      return 1.0F - f * f * f * f;
   }

   public static float getBrightness(DimensionType pDimensionType, int pLightLevel) {
      float f = (float)pLightLevel / 15.0F;
      float f1 = f / (4.0F - 3.0F * f);
      return Mth.lerp(pDimensionType.ambientLight(), f1, 1.0F);
   }

   public static int pack(int pBlockLight, int pSkyLight) {
      return pBlockLight << 4 | pSkyLight << 20;
   }

   public static int block(int pPackedLight) {
      return (pPackedLight & '\uffff') >> 4;
   }

   public static int sky(int pPackedLight) {
      return pPackedLight >> 20 & '\uffff';
   }

   private Vector3f getTempVector3f(float x, float y, float z) {
      this.tempVector.set(x, y, z);
      return this.tempVector;
   }

   private Vector3f getTempCopy(Vector3f vec) {
      this.tempVector.set(vec.x(), vec.y(), vec.z());
      return this.tempVector;
   }

   public boolean isAllowed() {
      return this.allowed;
   }

   public void setAllowed(boolean allowed) {
      this.allowed = allowed;
   }

   public boolean isCustom() {
      return this.custom;
   }

   public float getDarknessGammaFactor(float partialTicks) {
      float f = this.minecraft.options.darknessEffectScale().get().floatValue();
      return this.getDarknessGamma(partialTicks) * f;
   }

   public float getDarknessLightFactor(ClientLevel clientLevel, float partialTicks) {
      boolean flag = clientLevel.effects().forceBrightLightmap();
      if (flag) {
         return 0.0F;
      } else {
         float f = this.minecraft.options.darknessEffectScale().get().floatValue();
         float f1 = this.getDarknessGamma(partialTicks) * f;
         return this.calculateDarknessScale(this.minecraft.player, f1, partialTicks) * f;
      }
   }
}